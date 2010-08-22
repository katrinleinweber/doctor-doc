//  Copyright (C) 2005 - 2010  Markus Fischer, Pascal Steiner
//
//  This program is free software; you can redistribute it and/or
//  modify it under the terms of the GNU General Public License
//  as published by the Free Software Foundation; version 2 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//
//  Contact: info@doctor-doc.com

package ch.dbs.admin;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.grlea.log.SimpleLogger;

import util.MHelper;
import util.ReadSystemConfigurations;
import ch.dbs.entity.Billing;
import ch.dbs.entity.Konto;
import ch.dbs.entity.Text;
import ch.dbs.entity.Texttyp;
import ch.dbs.form.BillingForm;

/**
 * Erledigt administrative Tätigkeiten zu Abonomenten
 *
 * @author Pascal Steiner
 *
 */
public final class KontoAdmin  {

    private static final SimpleLogger LOG = new SimpleLogger(KontoAdmin.class);
    private static final int TWO_WEEKS = 14;
    private static final int TEN_DAYS = 14;

    /**
     * <p>Automatische Rechnungsstellung sowie Zahlungserinnerungen an Konten vor Konto-Ablaufdatum:</p>
     *
     * 2 Monate vor Ablauf wird die Rechnung erstellt und versendet<br>
     * 1 Monat vor Ablauf wird eine Erinnerung inklusive Rechnungsinformationen versendet<br>
     * 10 Tage vor Ablauf wird eine Warnung inklusive Rechnungsinformationen versendet<br><br>
     * @author Pascal Steiner
     */
    public void autoBillExpdate() {

        // Datumsformatierung vorbereiten
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd"); // Datumsformatierung für Vergleich
        SimpleDateFormat expdateformater = new SimpleDateFormat("dd.MM.yyyy");  // Datumsformatierung für Mailtext
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone(ReadSystemConfigurations.getSystemTimezone()));

        // Delta 2 Monate zu aktuellem Monat ausrechnen
        cal.add(Calendar.MONTH, +2);
        String expdatemysql = formater.format(cal.getTime()); // Datum fuer MYSQL vergleich
        cal.add(Calendar.DAY_OF_MONTH, -TWO_WEEKS);
        String lastpaydate2month = expdateformater.format(cal.getTime()); //Letzte Zahlungsmöglichkeit

        // Delta 1 Monat ausrechnen
        cal = new GregorianCalendar(TimeZone.getTimeZone(ReadSystemConfigurations.getSystemTimezone()));
        cal.add(Calendar.MONTH, +1);
        String expdatemysql1month = formater.format(cal.getTime()); // Datum fuer MYSQL vergleich
        cal.add(Calendar.DAY_OF_MONTH, -TWO_WEEKS);
        String lastpaydate1month = expdateformater.format(cal.getTime()); // Letzte Zahlungsmöglichkeit

        // 10 Tage vor Kontoablauf ausrechnen (Hinweis, dass es zu Unterbruch kommen kann)
        cal = new GregorianCalendar(TimeZone.getTimeZone(ReadSystemConfigurations.getSystemTimezone()));
        cal.add(Calendar.DAY_OF_MONTH, +TEN_DAYS);
        String warningdatemysql = formater.format(cal.getTime()); // Datum fuer MYSQL vergleich
        cal.add(Calendar.DAY_OF_MONTH, -TWO_WEEKS);
        String warninglastpaydate = expdateformater.format(cal.getTime()); // Letzte Zahlungsmöglichkeit vorbei am

        // Abgelaufenen Kontos deaktivieren !!CronJob Muss ca 10:30 laufen
        // (cal.add(Calendar.DAY_OF_MONTH, -1); geht nicht, sonst wird eine neue Rechnung erstellt!!
        cal = new GregorianCalendar(TimeZone.getTimeZone(ReadSystemConfigurations.getSystemTimezone()));
        //      cal.add(Calendar.DAY_OF_MONTH, -1);
        String expire = formater.format(cal.getTime()); // Datum fuer MYSQL vergleich

        // Datenbankverbindung aufbauen
        Connection cn = new Billing().getConnection();

        //    Alle Kontos holen
        List<Konto> expKontos = new Konto().getAllKontos(cn);

        // Kontos abarbeiten, ob eine Rechnung erstelt und versendet werden muss
        for (Konto k : expKontos) {
            if (k.getKontotyp() > 0) {
                if (k.getExpdate() != null) {

                    //           neue Rechnung erstellen, wenn das Abo in 2 Monaten ablauuft
                    if (k.getExpdate().toString().equals(expdatemysql)) {
                        this.newKontoAboBill(k, cn, lastpaydate2month);
                    }

                    // Zahlungserinnerung versenden wenn Abo in 1 Monat ablauuft
                    if (k.getExpdate().toString().equals(expdatemysql1month)) {
                        this.sendBillingReminder(k, cn, lastpaydate1month);
                    }

                    // Zahlungserinnerung versenden, wenn Abo in 10 Tagen ablauft
                    if (k.getExpdate().toString().equals(warningdatemysql)) {
                        this.sendBillingWarning(k, cn, warninglastpaydate);
                    }

                    // Bibliothekskonto deaktivieren, wenn Abo ohne Zahlungseingang abläuft
                    if (k.getExpdate().toString().equals(expire)) {

                        // Meldung an Kunden und Team Doctor-Doc senden
                        this.sendExpireMessage(k, cn);

                        //             Faxserver deaktivieren
                        k.setFaxno(null);
                        k.update(cn);
                    }
                }
            }

        }

        // DB Verbindung wieder schliessen
        try {
            cn.close();
        } catch (SQLException e) {
            LOG.error("autoBillExpdate()" + e.toString());
        }
    }

    /**
     * Rechnung erstellen und an Kunden versenden
     * @param k
     * @param cn
     * @param lastpaydate
     */
    public void newKontoAboBill(Konto k, Connection cn, String lastpaydate) {
        // Rechnung erstellen
        Billing b = null;
        //TODO: Nullpointer exception bei Kontotyp 1! noch beheben!
        //    if (k.getKontotyp() == 1)  { b = new Billing(k, new Text(cn, "1 Jahr "
        // + ReadSystemConfigurations.getApplicationName() + " Enhanced") , Double.valueOf("120"), "CHF"); }
        if (k.getKontotyp() == 2) { b = new Billing(k, new Text(cn, "1 Jahr "
                + ReadSystemConfigurations.getApplicationName()
                + " Enhanced plus Fax to Mail") , Double.valueOf("210"), "CHF");
        }
        if (k.getKontotyp() == 3) { b = new Billing(k, new Text(cn, "3 Monate "
                + ReadSystemConfigurations.getApplicationName()
                + " Enhanced plus Fax to Mail") , Double.valueOf("90"), "CHF");
        }

        if (b != null) {
            b.save(cn);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            // Mailtext vorbereiten
            BillingForm bf = new BillingForm();
            bf.setManuelltext("Ihr Faxserver auf doctor-doc.com läuft am " + sdf.format(k.getExpdate()) + " ab.");
            bf.setBillingtext("\n\nUm weiterhin optimal von "
                    + ReadSystemConfigurations.getApplicationName()
                    + " profitieren zu können, muss der Betrag von "
                    + b.getBetrag() + " " + b.getWaehrung()  + " bis spätestens am "
                    + lastpaydate + " auf dem Konto des "
                    + "Vereins " + ReadSystemConfigurations.getApplicationName() + " eintreffen.");
            bf = this.prepareBillingText(k, cn, lastpaydate, bf);

            // Mail senden
            MHelper mh = new MHelper();
            String[] to = new String[1];
            to[0] = k.getBibliotheksmail();
            if (b.getId() != null) {
                mh.sendMail(to, "RECHNUNG: Erneuerung Ihres Faxservers bei "
                        + ReadSystemConfigurations.getApplicationName() + ". Ablaufdatum: "
                        + sdf.format(k.getExpdate()), bf.getBillingtext());
            } else { // Hinweis senden falls keine Rechnung erstellt werden konnte
                mh.sendErrorMail("WARNUNG: Folgende Rechnung konnte nicht erstellt werden, Kunde "
                        + k.getBibliotheksname()
                        + " ist nicht benachrichtigt worden! Es wurde keine Rechnung gespeichert!",
                        "Die Rechnung konnte nicht gespeichert werden. Vorbereiteter Mailtext:\n\n"
                        + bf.getBillingtext());
            }

        }
    }


    /**
     * Rechnungstext vorbereiten / korrigieren
     *
     *
     * @param k
     * @param cn
     * @param BillingForm
     * @param lastpaydate
     */
    public BillingForm prepareBillingText(Konto k, Connection cn, String lastpaydate, BillingForm bf) {

        if (bf == null) {
            bf = new BillingForm();
        }

        // Rechnungsobjekt vorbereiten, noch nicht speichern
        // da dies erst geschieht wenn die Rechnung wirklich versendet wird.
        Billing b = new Billing();
        b = b.getLastBilling(k, cn);
        if (b == null) {
            if (k.getKontotyp() == 0 || k.getKontotyp() == 1) { b = new Billing(k,
                    new Text(cn, "Dienstleistung") , Double.valueOf("0"), "CHF");
            }
            if (k.getKontotyp() == 2) { b = new Billing(k,
                    new Text(cn, "1 Jahr " + ReadSystemConfigurations.getApplicationName()
                            + " Enhanced plus Fax to Mail") , Double.valueOf("210"), "CHF");
            }
            if (k.getKontotyp() == 3) { b = new Billing(k,
                    new Text(cn, "3 Monate " + ReadSystemConfigurations.getApplicationName()
                            + " Enhanced plus Fax to Mail") , Double.valueOf("90"), "CHF");
            }
        }

        //Betrag aendern
        if (bf.getBetrag() != 0.0) {
            b.setBetrag(bf.getBetrag());
        }

        //Waehrung aendern
        if (bf.getWaehrung() != null) {
            if (!bf.getWaehrung().equals("")) {
                b.setWaehrung(bf.getWaehrung());
            }
        }

        //    Rechnungsdatum aendern
        if (bf.getRechnungsdatum() != null) {
            if (!bf.getWaehrung().equals("")) {
                b.setRechnungsdatum(bf.getRechnungsdatum());
            }
        }

        // Rechnungsgrund aendern
        if (bf.getRechnungsgrundid() != null) {
            if (bf.getRechnungsgrundid() != 0) {
                b.setRechnungsgrund(new Text(cn, bf.getRechnungsgrundid()));
            }
        }

        bf.setBill(b);

        //Rechnungseinleitung vorbereiten
        StringBuffer t = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        t.append("RECHNUNG" + "\n" + "\n");
        t.append("Sehr geehrte Damen und Herren");

        //Optionaler Rechnungstext ergänzen (nur für Mail, wird nicht gespeichert
        if (bf.getManuelltext() != null) {
            if (!bf.getManuelltext().equals("")) {
                t.append("\n\n" + bf.getManuelltext());
            } else {
                // Bestehende Kunden auf das Ablaufdatum des Kontos hinweisen, nur wenn
                // kein manueller Rechnungstext ergänzt werden soll
                if (k.getKontotyp() != 0) {
                    if (k.getExpdate() != null) {
                        t.append("\n\nIhr Faxserver auf doctor-doc.com läuft am "
                                + sdf.format(k.getExpdate()) + " ab.");
                        t.append("\n\nBitte  überweisen Sie den Betrag innert 30 Tagen und mit "
                                + "Angabe der Rechnungsnummer." + "\n");
                    }
                }
            }
        }


        t.append("\n\nRechnungsinformationen:" + "\n");
        t.append("Betrag: " + b.getBetrag() + " " + b.getWaehrung() + "\n");
        t.append("Rechnungsnummer: " + b.getRechnungsnummer() +  "\n");
        t.append("Rechnungsdatum: " + sdf.format(b.getRechnungsdatum()) + "\n");
        t.append("Rechnungsgrund: " + b.getRechnungsgrund().getInhalt());

        //    Zahlungsinformationen hinzufügen
        t.append(getBillingInfos());

        // Hinweis Zahlungskreuzung einschieben
        if (bf.getBillingtext() != null) {
            if (!bf.getBillingtext().equals("")) {
                t.append("\n\n" + bf.getBillingtext());
            }
        }
        t.append("\n\nFreundliche Grüsse:");
        t.append("\n\nIhr Team " + ReadSystemConfigurations.getApplicationName());

        Text txt = new Text();
        bf.setRechnungsgrundliste(txt.getText(new Texttyp("Rechnungsgrund", cn), cn));

        bf.setBillingtext(t.toString());

        return bf;
    }

    /**
     * Zahlungserinnerung versenden
     * @param k
     * @param cn
     * @param lastpaydate
     */
    public void sendBillingReminder(Konto k, Connection cn, String lastpaydate) {

        // Offene Rechnung heraussuchen
        Billing b = new Billing();
        b = b.getLastBilling(k, cn);

        //       Mailtext vorbereiten
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        BillingForm bf = new BillingForm();

        MHelper mh = new MHelper();
        String[] to = new String[1];
        to[0] = k.getBibliotheksmail();

        if (b != null) {
            bf.setManuelltext("Ihr Konto auf doctor-doc.com läuft am " + sdf.format(k.getExpdate()) + " ab.");
            bf.setBillingtext("\n\nUm weiterhin vom Faxserver auf "
                    + ReadSystemConfigurations.getApplicationName() + " profitieren zu können, muss der Betrag von "
                    + b.getBetrag() + " " + b.getWaehrung()  + " bis spätestens am "
                    + lastpaydate + " auf dem Konto des Vereins "
                    + ReadSystemConfigurations.getApplicationName() + " eintreffen."
                    + "\n\nSollte sich ihre Zahlung mit dieser Mail gekreuzt haben, betrachten Sie dieses Mail "
                    + "bitte als gegenstandslos."); // Hinweis Zahlungskreuzung, laspaydate
            bf = this.prepareBillingText(k, cn, lastpaydate, bf);

            // Rechnung versenden


            mh.sendMail(to, "Hinweis: Ihr Faxserver auf doctor-doc.com laeuft am "
                    + sdf.format(k.getExpdate()) + " ab", bf.getBillingtext());
        } else { // Hinweis versenden wenn keine offene Zahlung gefunden wurde
            mh.sendErrorMail("ZAHLUNGSERINNERUNG 30 Tage vor Ablauf: von Kunde "
                    + k.getBibliotheksname() + " musste nicht versendet werden.",
                    "Keine offene Rechnung gefunden, alles bezahlt!\n\nBitte Kontoablaufdatum noch richtig setzen, "
                    + "das ist vermutlich beim Zahlungseingang vergessen gegangen... ");
        }
    }

    /**
     * Warntext versenden
     * @param k
     * @param cn
     * @param expdate
     * @param lastpaydate
     */
    public void sendBillingWarning(Konto k, Connection cn, String lastpaydate) {

        //       Offene Rechnung heraussuchen
        Billing b = new Billing();
        b = b.getLastBilling(k, cn);

        //      Mailtext vorbereiten
        BillingForm bf = new BillingForm();

        MHelper mh = new MHelper();
        String[] to = new String[1];
        to[0] = k.getBibliotheksmail();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        if (b != null) {
            bf.setManuelltext("Wir konnten leider keinen rechtzeitigen Zahlungseingang ihres ausstehenden Betrages bei "
                    + "uns verzeichnen. Wir weisen Sie darauf hin, dass wir aus administrativen Gründen nicht "
                    + "gewährleisten können, dass Ihnen ihr Faxserver unterbrechungsfrei zur Verfügung steht. "
                    + "Sobald der offene Betrag bei uns eingegangen ist, reaktivieren wir Ihnen Ihr, in der "
                    + "Zwischenzeit ggf. deaktivierter, Faxserver auf doctor-doc.com wieder");
            bf.setBillingtext("Sollte sich ihre Zahlung mit dieser Mail gekreuzt haben, betrachten Sie dieses Mail "
                    + "bitte als gegenstandslos."); // Hinweis Zahlungskreuzung
            bf = this.prepareBillingText(k, cn, lastpaydate, bf);

            //       Rechnung versenden
            mh.sendMail(to, "Hinweis: Ihr Faxserver auf doctor-doc.com laeuft ab am "
                    + sdf.format(k.getExpdate()), bf.getBillingtext());
        } else {  // Hinweis versenden wenn keine offene Zahlung gefunden wurde
            mh.sendErrorMail("LETZTE ZAHLUNGSERINNERUNG: von Kunde " + k.getBibliotheksname()
                    + " musste nicht versendet werden.!",
                    "Keine offene Rechnung gefunden, alles bezahlt!\n\nBitte Kontoablaufdatum noch richtig setzen, "
                    + "das ist vermutlich beim Zahlungseingang vergessen gegangen... ");
        }
    }

    /**
     * Versendet die Meldung dass das Bibliothekskonto deaktiviert wurde inklusive nochmals Zahlungsdetails
     * und dass das Konto bei Zahlungseingang wieder reaktiviert wird.
     *
     * @param Konto
     */
    public void sendExpireMessage(Konto k, Connection cn) {

        //       Offene Rechnung heraussuchen
        Billing b = new Billing();
        b = b.getLastBilling(k, cn);

        // Hinweismeldung vorbereiten
        BillingForm bf = new BillingForm();
        if (b != null) {
            bf.setManuelltext("Der Faxserver ihres Bibliothekskonto wurde automatisch deaktiviert, da kein "
                    + "rechtzeitiger Zahlungseingang der unten stehenden Rechnung verzeichnet werden konnte. "
                    + "Sobald die Rechnung beglichen wird, können wir ihren Faxserver wieder freischalten "
                    + "(Keine Nummergarantie).");
            bf = this.prepareBillingText(k, cn, null, bf);
            //      Eigentlich sollte man nie ohne offene Rechnung hier landen, ansonsten ist vermutlich
            // das Expire Date des Kontos falsch gesetzt
        } else {
            bf.setBillingtext("Sehr geehrte Damen und Herren\n\n"
                    + "Leider mussten wir aufgrund eines internen Fehlers in "
                    + ReadSystemConfigurations.getApplicationName() + " ihren Faxserver deaktivieren. "
                    + "Wir bemühen uns, diesen Fehler schnellstmöglich zu beheben um den Faxserver wieder frei "
                    + "geben zu können.\n\n"
                    + "Freundliche Grüsse:\n\n"
                    + "Ihr Team " + ReadSystemConfigurations.getApplicationName());
        }

        // Hinweis an Kunden versenden
        MHelper mh = new MHelper();
        String[] to = new String[1];
        to[0] = k.getBibliotheksmail();
        mh.sendMail(to, "Hinweis: Ihr Faxserver auf doctor-doc.com wurde deaktiviert.", bf.getBillingtext());

        // Hinweis an Team Doctor-Doc versenden
        mh.sendErrorMail("Hinweis: Der Faxserver vom Konto " + k.getBibliotheksname()
                + " auf doctor-doc.com wurde deaktiviert.",
                "Vermutete Ursache: konto Ablaufdatum falsch gesetzt. Folgender Mailtext wurde verschickt:\n\n"
                + bf.getBillingtext());

    }


    public String getBillingInfos() {

        StringBuffer t = new StringBuffer();
        t.append("\n\n*******************************************************" + "\n");
        t.append("Zahlungsinformationen:" + "\n");
        t.append(ReadSystemConfigurations.getApplicationName() + "\n");
        t.append("Konto Nr.:      16 9.350.8040.02" + "\n");
        t.append("IBAN:           CH62 0630 0016 9350 8040 2" + "\n");
        t.append("Bank:           Valiant Bank, Postfach, 3001 Bern" + "\n");
        t.append("Bankenclearing: 6300" + "\n");
        t.append("Postcheckkonto: 30-38112-0" + "\n");
        t.append("Swift-Code:     VABECH22" + "\n\n");
        t.append("Name und Adresse:\n");
        t.append("Pascal Steiner" + "\n");
        t.append("Gysulastrasse 83" + "\n");
        t.append("5022 Rombach (CH)" + "\n");
        t.append("*******************************************************");

        return t.toString();
    }
}
