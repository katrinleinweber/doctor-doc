//  Copyright (C) 2011  Markus Fischer, Pascal Steiner
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

package ch.dbs.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.struts.validator.ValidatorForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.CodeUrl;
import ch.dbs.actions.openurl.ContextObject;
import ch.dbs.actions.openurl.ConvertOpenUrl;
import ch.dbs.actions.openurl.OpenUrl;
import ch.dbs.form.OrderForm;

/**
 * Class to transmit DAIA requests to an (external) order form. <p></p>
 * 
 * @author Markus Fischer
 */
public class DaiaParam extends ValidatorForm {

    private static final Logger LOG = LoggerFactory.getLogger(DaiaParam.class);
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long kid;
    private String baseurl;
    private String linkAGB;
    private String linkFees;
    private String firstParam = "?";
    private String protocol = "custom"; // internal / openurl / custom
    private boolean redirect = true; // defaults to true
    private boolean post; // defaults to false
    private boolean ip_overrides; // defaults to false
    private String mapMediatype;
    private String mapAuthors;
    private String mapAtitle;
    private String mapBtitle;
    private String mapChapter;
    private String mapJournal;
    private String mapPublisher;
    private String mapIssn;
    private String mapIsbn;
    private String mapDate;
    private String mapVolume;
    private String mapIssue;
    private String mapPages;
    private String mapPmid;
    private String mapDoi;
    private String mapReference;
    private String referenceValue;
    private String limitations;

    // additional paramateres not from database

    private boolean worldWideAccess;

    private String source;
    private String sourceValue;
    private String identification;
    private String password;
    private String free1;
    private String free1Value;
    private String free2;
    private String free2Value;
    private String free3;
    private String free3Value;
    private String free4;
    private String free4Value;
    private String free5;
    private String free5Value;
    private String free6;
    private String free6Value;
    private String free7;
    private String free7Value;
    private String free8;
    private String free8Value;
    private String free9;
    private String free9Value;

    // linkout composed upon baseurl and protocol
    private String linkout;

    public DaiaParam() {

    }

    public DaiaParam(final Long did, final Connection cn) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = cn.prepareStatement("SELECT * FROM bestellform_daia WHERE DID=?");
            pstmt.setLong(1, did);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                getRsValues(rs);
            }

        } catch (final Exception e) {
            LOG.error("DaiaParam(final Long did, final Connection cn): " + e.toString());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    LOG.error("DaiaParam(final Long did, final Connection cn): " + e.toString());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    LOG.error("DaiaParam(final Long did, final Connection cn): " + e.toString());
                }
            }
        }

    }

    public DaiaParam(final Konto konto, final Connection cn) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = cn.prepareStatement("SELECT * FROM bestellform_daia WHERE KID=?");
            pstmt.setLong(1, konto.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                getRsValues(rs);
            }

            // set visibility for modifications
            final Konto k = new Konto(konto.getId(), cn);
            if (k.getDid() != null) {
                this.setWorldWideAccess(true);
            }

        } catch (final Exception e) {
            LOG.error("DaiaParam(final Konto konto, final Connection cn): " + e.toString());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    LOG.error("DaiaParam(final Konto konto, final Connection cn): " + e.toString());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    LOG.error("DaiaParam(final Konto konto, final Connection cn): " + e.toString());
                }
            }
        }

    }

    /**
     * Saves a DaiamParam into the database.
     * 
     * @param Konto konto
     * @param Connection cn
     */
    public Long save(final Konto konto, final Connection cn) {

        Long pbId = null;

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {

            // set KID to DaiaParam
            this.setKid(konto.getId());

            pstmt = setDaiaParamValues(cn
                    .prepareStatement("INSERT INTO `bestellform_daia` (`KID` , `baseurl` , `link_agb` , "
                            + "`link_fees` , `first_param` , `protocol` , `redirect` , `post` , `ip_overrides` , "
                            + "`map_mediatype` , `map_authors` , `map_atitle` , `map_btitle` , `map_chapter` , "
                            + "`map_journal` , `map_publisher` , `map_issn` , `map_isbn` , `map_date` , `map_volume` , "
                            + "`map_issue` , `map_pages` , `map_pmid` , `map_doi` , `map_reference` , `limitations`) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"));

            pstmt.executeUpdate();

            // get back last inserted ID
            rs = pstmt.executeQuery("SELECT LAST_INSERT_ID()");
            if (rs.next()) {
                pbId = rs.getLong("LAST_INSERT_ID()");
                this.setId(pbId);
            }

            // set visibility to the account
            if (this.isWorldWideAccess()) {
                updateVisibility(this.getId(), cn);
            } else {
                updateVisibility(null, cn);
            }

        } catch (final Exception e) {
            LOG.error("save(Connection cn): " + e.toString());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    LOG.error(e.toString());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    LOG.error(e.toString());
                }
            }
        }

        return pbId;
    }

    /**
     * Deletes a all DaiamParams from the database for an account.
     * 
     * @param Konto konto
     * @param Connection cn
     */
    public void delete(final Konto konto, final Connection cn) {

        PreparedStatement pstmt = null;
        try {

            // set KID to DaiaParam
            this.setKid(konto.getId());

            pstmt = cn.prepareStatement("DELETE FROM `bestellform_daia` WHERE `KID` =?");
            pstmt.setLong(1, konto.getId());
            pstmt.executeUpdate();

            // set visibility to null
            updateVisibility(null, cn);

        } catch (final Exception e) {
            LOG.error("delete(final Konto konto, final Connection cn): " + e.toString());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    LOG.error("delete(final Konto konto, final Connection cn): " + e.toString());
                }
            }
        }
    }

    private void updateVisibility(final Long id2, final Connection cn) {

        PreparedStatement pstmt = null;
        try {
            pstmt = cn.prepareStatement("UPDATE `konto` SET DID=? " + "WHERE `KID` = ?");

            if (id2 != null) {
                pstmt.setLong(1, id2);
            } else {
                pstmt.setString(1, null);
            }
            pstmt.setLong(2, this.getKid());

            pstmt.executeUpdate();

        } catch (final Exception e) {
            LOG.error("updateVisibility(final Long id2, final Connection cn): " + e.toString());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    LOG.error(e.toString());
                }
            }
        }
    }

    private PreparedStatement setDaiaParamValues(final PreparedStatement pstmt) throws Exception {

        pstmt.setLong(1, this.getKid());
        // remove possible empty characters at the end
        pstmt.setString(2, this.getBaseurl().trim());
        if (isEmpty(this.getLinkAGB())) {
            pstmt.setString(3, "");
        } else {
            pstmt.setString(3, this.getLinkAGB().trim());
        }
        if (isEmpty(this.getLinkFees())) {
            pstmt.setString(4, "");
        } else {
            pstmt.setString(4, this.getLinkFees().trim());
        }
        pstmt.setString(5, this.getFirstParam());
        pstmt.setString(6, this.getProtocol());
        pstmt.setBoolean(7, this.isRedirect());
        pstmt.setBoolean(8, this.isPost());
        pstmt.setBoolean(9, this.isIp_overrides());
        if (isEmpty(this.getMapMediatype())) {
            pstmt.setString(10, null);
        } else {
            pstmt.setString(10, this.getMapMediatype().trim());
        }
        if (isEmpty(this.getMapAuthors())) {
            pstmt.setString(11, null);
        } else {
            pstmt.setString(11, this.getMapAuthors().trim());
        }
        if (isEmpty(this.getMapAtitle())) {
            pstmt.setString(12, null);
        } else {
            pstmt.setString(12, this.getMapAtitle().trim());
        }
        if (isEmpty(this.getMapBtitle())) {
            pstmt.setString(13, null);
        } else {
            pstmt.setString(13, this.getMapBtitle().trim());
        }
        if (isEmpty(this.getMapChapter())) {
            pstmt.setString(14, null);
        } else {
            pstmt.setString(14, this.getMapChapter().trim());
        }
        if (isEmpty(this.getMapJournal())) {
            pstmt.setString(15, null);
        } else {
            pstmt.setString(15, this.getMapJournal().trim());
        }
        if (isEmpty(this.getMapPublisher())) {
            pstmt.setString(16, null);
        } else {
            pstmt.setString(16, this.getMapPublisher().trim());
        }
        if (isEmpty(this.getMapIssn())) {
            pstmt.setString(17, null);
        } else {
            pstmt.setString(17, this.getMapIssn().trim());
        }
        if (isEmpty(this.getMapIsbn())) {
            pstmt.setString(18, null);
        } else {
            pstmt.setString(18, this.getMapIsbn().trim());
        }
        if (isEmpty(this.getMapDate())) {
            pstmt.setString(19, null);
        } else {
            pstmt.setString(19, this.getMapDate().trim());
        }
        if (isEmpty(this.getMapVolume())) {
            pstmt.setString(20, null);
        } else {
            pstmt.setString(20, this.getMapVolume().trim());
        }
        if (isEmpty(this.getMapIssue())) {
            pstmt.setString(21, null);
        } else {
            pstmt.setString(21, this.getMapIssue().trim());
        }
        if (isEmpty(this.getMapPages())) {
            pstmt.setString(22, null);
        } else {
            pstmt.setString(22, this.getMapPages().trim());
        }
        if (isEmpty(this.getMapPmid())) {
            pstmt.setString(23, null);
        } else {
            pstmt.setString(23, this.getMapPmid().trim());
        }
        if (isEmpty(this.getMapDoi())) {
            pstmt.setString(24, null);
        } else {
            pstmt.setString(24, this.getMapDoi().trim());
        }
        if (isEmpty(this.getMapReference())) {
            pstmt.setString(25, null);
        } else {
            pstmt.setString(25, this.getMapReference().trim());
        }
        if (isEmpty(this.getLimitations())) {
            pstmt.setString(26, "");
        } else {
            // the value comes from a text area => trim
            pstmt.setString(26, this.getLimitations().trim());
        }

        return pstmt;
    }

    private void getRsValues(final ResultSet rs) throws Exception {

        this.setId(rs.getLong("DID"));
        this.setKid(rs.getLong("KID"));
        this.setBaseurl(rs.getString("baseurl"));
        this.setLinkAGB(rs.getString("link_agb"));
        this.setLinkFees(rs.getString("link_fees"));
        this.setFirstParam(rs.getString("first_param"));
        this.setProtocol(rs.getString("protocol"));
        this.setRedirect(rs.getBoolean("redirect"));
        this.setPost(rs.getBoolean("post"));
        this.setIp_overrides(rs.getBoolean("ip_overrides"));
        this.setMapMediatype(rs.getString("map_mediatype"));
        this.setMapAuthors(rs.getString("map_authors"));
        this.setMapAtitle(rs.getString("map_atitle"));
        this.setMapBtitle(rs.getString("map_btitle"));
        this.setMapChapter(rs.getString("map_chapter"));
        this.setMapJournal(rs.getString("map_journal"));
        this.setMapPublisher(rs.getString("map_publisher"));
        this.setMapIssn(rs.getString("map_issn"));
        this.setMapIsbn(rs.getString("map_isbn"));
        this.setMapDate(rs.getString("map_date"));
        this.setMapVolume(rs.getString("map_volume"));
        this.setMapIssue(rs.getString("map_issue"));
        this.setMapPages(rs.getString("map_pages"));
        this.setMapPmid(rs.getString("map_pmid"));
        this.setMapDoi(rs.getString("map_doi"));
        this.setMapReference(rs.getString("map_reference"));
        this.setLimitations(rs.getString("limitations"));

    }

    public void setLinkout(final DaiaParam dp, final OrderForm of) {

        final StringBuffer link = new StringBuffer(dp.getBaseurl());

        // set combined reference
        if (dp.getMapReference() != null) {
            dp.setReferenceValue(combineReference(of));
        }

        // do append parameters to baseurl only for GET method
        if (!dp.isPost()) {

            link.append(dp.getFirstParam());

            if ("openurl".equals(dp.getProtocol())) {
                linkoutOpenURL(link, of);
            } else if ("custom".equals(dp.getProtocol())) {
                linkoutCustom(link, dp, of);
            } else { // default value "internal"
                linkoutInternal(link, of);
            }
        }

        dp.setLinkout(link.toString());
    }

    private void linkoutOpenURL(final StringBuffer link, final OrderForm of) {

        // create a new OpenURL object
        final ContextObject co = new ConvertOpenUrl().makeContextObject(of, "UTF-8");
        final String openurl = new OpenUrl().composeOpenUrl(co);
        link.append(openurl);

    }

    private void linkoutCustom(final StringBuffer link, final DaiaParam dp, final OrderForm of) {

        final CodeUrl url = new CodeUrl();

        if (of.getMediatype() != null && !"".equals(of.getMediatype()) && dp.getMapMediatype() != null) {
            link.append(dp.getMapMediatype());
            link.append('=');
            link.append(url.encode(of.getMediatype(), "UTF-8"));
        }
        if (of.getJahr() != null && !"".equals(of.getJahr()) && dp.getMapDate() != null) {
            appendParam(link);
            link.append(dp.getMapDate());
            link.append('=');
            link.append(url.encode(of.getJahr(), "UTF-8"));
        }
        if (of.getJahrgang() != null && !"".equals(of.getJahrgang()) && dp.getMapVolume() != null) {
            appendParam(link);
            link.append(dp.getMapVolume());
            link.append('=');
            link.append(url.encode(of.getJahrgang(), "UTF-8"));
        }
        if (of.getHeft() != null && !"".equals(of.getHeft()) && dp.getMapIssue() != null) {
            appendParam(link);
            link.append(dp.getMapIssue());
            link.append('=');
            link.append(url.encode(of.getHeft(), "UTF-8"));
        }
        if (of.getSeiten() != null && !"".equals(of.getSeiten()) && dp.getMapPages() != null) {
            appendParam(link);
            link.append(dp.getMapPages());
            link.append('=');
            link.append(url.encode(of.getSeiten(), "UTF-8"));
        }
        // no equivalence in orderform. ReferenceValue has been set before.
        if (dp.getMapReference() != null && dp.getReferenceValue() != null) {
            appendParam(link);
            link.append(dp.getMapReference());
            link.append('=');
            link.append(url.encode(dp.getReferenceValue(), "UTF-8"));
        }
        if (of.getIssn() != null && !"".equals(of.getIssn()) && dp.getMapIssn() != null) {
            appendParam(link);
            link.append(dp.getMapIssn());
            link.append('=');
            link.append(url.encode(of.getIssn(), "UTF-8"));
        }
        if (of.getIsbn() != null && !"".equals(of.getIsbn()) && dp.getMapIsbn() != null) {
            appendParam(link);
            link.append(dp.getMapIsbn());
            link.append('=');
            link.append(url.encode(of.getIsbn(), "UTF-8"));
        }
        if (of.getZeitschriftentitel() != null && !"".equals(of.getZeitschriftentitel()) && dp.getMapJournal() != null) {
            appendParam(link);
            link.append(dp.getMapJournal());
            link.append('=');
            link.append(url.encode(of.getZeitschriftentitel(), "UTF-8"));
        }
        if (of.getArtikeltitel() != null && !"".equals(of.getArtikeltitel()) && dp.getMapAtitle() != null) {
            appendParam(link);
            link.append(dp.getMapAtitle());
            link.append('=');
            link.append(url.encode(of.getArtikeltitel(), "UTF-8"));
        }
        if (of.getAuthor() != null && !"".equals(of.getAuthor()) && dp.getMapAuthors() != null) {
            appendParam(link);
            link.append(dp.getMapAuthors());
            link.append('=');
            link.append(url.encode(of.getAuthor(), "UTF-8"));
        }
        if (of.getBuchtitel() != null && !"".equals(of.getBuchtitel()) && dp.getMapBtitle() != null) {
            appendParam(link);
            link.append(dp.getMapBtitle());
            link.append('=');
            link.append(url.encode(of.getBuchtitel(), "UTF-8"));
        }
        if (of.getKapitel() != null && !"".equals(of.getKapitel()) && dp.getMapChapter() != null) {
            appendParam(link);
            link.append(dp.getMapChapter());
            link.append('=');
            link.append(url.encode(of.getKapitel(), "UTF-8"));
        }
        if (of.getVerlag() != null && !"".equals(of.getVerlag()) && dp.getMapPublisher() != null) {
            appendParam(link);
            link.append(dp.getMapPublisher());
            link.append('=');
            link.append(url.encode(of.getVerlag(), "UTF-8"));
        }
        if (of.getPmid() != null && !"".equals(of.getPmid()) && dp.getMapPmid() != null) {
            appendParam(link);
            link.append(dp.getMapPmid());
            link.append('=');
            link.append(url.encode(of.getPmid(), "UTF-8"));
        }
        if (of.getDoi() != null && !"".equals(of.getDoi()) && dp.getMapDoi() != null) {
            appendParam(link);
            link.append(dp.getMapDoi());
            link.append('=');
            link.append(url.encode(of.getDoi(), "UTF-8"));
        }

    }

    private void linkoutInternal(final StringBuffer link, final OrderForm of) {

        link.append("mediatype=");
        link.append(of.getMediatype());
        if (of.getJahr() != null && !"".equals(of.getJahr())) {
            link.append("&jahr=");
            link.append(of.getJahr());
        }
        if (of.getJahrgang() != null && !"".equals(of.getJahrgang())) {
            link.append("&jahrgang=");
            link.append(of.getJahrgang());
        }
        if (of.getHeft() != null && !"".equals(of.getHeft())) {
            link.append("&heft=");
            link.append(of.getHeft());
        }
        if (of.getSeiten() != null && !"".equals(of.getSeiten())) {
            link.append("&seiten=");
            link.append(of.getSeiten());
        }
        if (of.getIssn() != null && !"".equals(of.getIssn())) {
            link.append("&issn=");
            link.append(of.getIssn());
        }
        if (of.getIsbn() != null && !"".equals(of.getIsbn())) {
            link.append("&isbn=");
            link.append(of.getIsbn());
        }
        if (of.getZeitschriftentitel() != null && !"".equals(of.getZeitschriftentitel())) {
            link.append("&zeitschriftentitel=");
            link.append(of.getZeitschriftentitel());
        }
        if (of.getArtikeltitel() != null && !"".equals(of.getArtikeltitel())) {
            link.append("&artikeltitel=");
            link.append(of.getArtikeltitel());
        }
        if (of.getAuthor() != null && !"".equals(of.getAuthor())) {
            link.append("&author=");
            link.append(of.getAuthor());
        }
        if (of.getBuchtitel() != null && !"".equals(of.getBuchtitel())) {
            link.append("&buchtitel=");
            link.append(of.getBuchtitel());
        }
        if (of.getKapitel() != null && !"".equals(of.getKapitel())) {
            link.append("&kapitel=");
            link.append(of.getKapitel());
        }
        if (of.getVerlag() != null && !"".equals(of.getVerlag())) {
            link.append("&verlag=");
            link.append(of.getVerlag());
        }
        if (of.getPmid() != null && !"".equals(of.getPmid())) {
            link.append("&pmid=");
            link.append(of.getPmid());
        }
        if (of.getDoi() != null && !"".equals(of.getDoi())) {
            link.append("&doi=");
            link.append(of.getDoi());
        }

    }

    private void appendParam(final StringBuffer buf) {

        // check to see if we already have an ? or an & at the end
        if (buf.length() > 0 && buf.charAt(buf.length() - 1) != '?' && buf.charAt(buf.length() - 1) != '&') {
            buf.append('&'); // if not, append
        }

    }

    public String combineReference(final OrderForm of) {
        final StringBuffer ref = new StringBuffer();

        if (of.getJahr() != null) {
            ref.append(of.getJahr());
        }
        ref.append(';');
        if (of.getJahrgang() != null) {
            ref.append(of.getJahrgang());
        }
        ref.append('(');
        if (of.getHeft() != null) {
            ref.append(of.getHeft());
        }
        ref.append(')');
        ref.append(':');
        if (of.getSeiten() != null) {
            ref.append(of.getSeiten());
        }

        // no reference at all
        if (";():".equals(ref.toString())) {
            return "";
        }

        return ref.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getKid() {
        return kid;
    }

    public void setKid(final Long kid) {
        this.kid = kid;
    }

    public String getBaseurl() {
        return baseurl;
    }

    public void setBaseurl(final String baseurl) {
        this.baseurl = baseurl;
    }

    public String getLinkAGB() {
        return linkAGB;
    }

    public void setLinkAGB(final String linkAGB) {
        this.linkAGB = linkAGB;
    }

    public String getLinkFees() {
        return linkFees;
    }

    public void setLinkFees(final String linkFees) {
        this.linkFees = linkFees;
    }

    public String getFirstParam() {
        return firstParam;
    }

    public void setFirstParam(final String firstParam) {
        this.firstParam = firstParam;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(final boolean redirect) {
        this.redirect = redirect;
    }

    public boolean isPost() {
        return post;
    }

    public void setPost(final boolean post) {
        this.post = post;
    }

    public boolean isIp_overrides() {
        return ip_overrides;
    }

    public void setIp_overrides(final boolean ipOverrides) {
        ip_overrides = ipOverrides;
    }

    public String getMapMediatype() {
        return mapMediatype;
    }

    public void setMapMediatype(final String mapMediatype) {
        this.mapMediatype = mapMediatype;
    }

    public String getMapAuthors() {
        return mapAuthors;
    }

    public void setMapAuthors(final String mapAuthors) {
        this.mapAuthors = mapAuthors;
    }

    public String getMapAtitle() {
        return mapAtitle;
    }

    public void setMapAtitle(final String mapAtitle) {
        this.mapAtitle = mapAtitle;
    }

    public String getMapBtitle() {
        return mapBtitle;
    }

    public void setMapBtitle(final String mapBtitle) {
        this.mapBtitle = mapBtitle;
    }

    public String getMapChapter() {
        return mapChapter;
    }

    public void setMapChapter(final String mapChapter) {
        this.mapChapter = mapChapter;
    }

    public String getMapJournal() {
        return mapJournal;
    }

    public void setMapJournal(final String mapJournal) {
        this.mapJournal = mapJournal;
    }

    public String getMapPublisher() {
        return mapPublisher;
    }

    public void setMapPublisher(final String mapPublisher) {
        this.mapPublisher = mapPublisher;
    }

    public String getMapIssn() {
        return mapIssn;
    }

    public void setMapIssn(final String mapIssn) {
        this.mapIssn = mapIssn;
    }

    public String getMapIsbn() {
        return mapIsbn;
    }

    public void setMapIsbn(final String mapIsbn) {
        this.mapIsbn = mapIsbn;
    }

    public String getMapDate() {
        return mapDate;
    }

    public void setMapDate(final String mapDate) {
        this.mapDate = mapDate;
    }

    public String getMapVolume() {
        return mapVolume;
    }

    public void setMapVolume(final String mapVolume) {
        this.mapVolume = mapVolume;
    }

    public String getMapIssue() {
        return mapIssue;
    }

    public void setMapIssue(final String mapIssue) {
        this.mapIssue = mapIssue;
    }

    public String getMapPages() {
        return mapPages;
    }

    public void setMapPages(final String mapPages) {
        this.mapPages = mapPages;
    }

    public String getMapPmid() {
        return mapPmid;
    }

    public void setMapPmid(final String mapPmid) {
        this.mapPmid = mapPmid;
    }

    public String getMapDoi() {
        return mapDoi;
    }

    public void setMapDoi(final String mapDoi) {
        this.mapDoi = mapDoi;
    }

    public String getMapReference() {
        return mapReference;
    }

    public void setMapReference(final String mapReference) {
        this.mapReference = mapReference;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(final String referenceValue) {
        this.referenceValue = referenceValue;
    }

    public String getLinkout() {
        return linkout;
    }

    public void setLinkout(final String linkout) {
        this.linkout = linkout;
    }

    public String getLimitations() {
        return limitations;
    }

    public void setLimitations(final String limitations) {
        this.limitations = limitations;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(final String identification) {
        this.identification = identification;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(final String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public String getFree1() {
        return free1;
    }

    public void setFree1(final String free1) {
        this.free1 = free1;
    }

    public String getFree1Value() {
        return free1Value;
    }

    public void setFree1Value(final String free1Value) {
        this.free1Value = free1Value;
    }

    public String getFree2() {
        return free2;
    }

    public void setFree2(final String free2) {
        this.free2 = free2;
    }

    public String getFree2Value() {
        return free2Value;
    }

    public void setFree2Value(final String free2Value) {
        this.free2Value = free2Value;
    }

    public String getFree3() {
        return free3;
    }

    public void setFree3(final String free3) {
        this.free3 = free3;
    }

    public String getFree3Value() {
        return free3Value;
    }

    public void setFree3Value(final String free3Value) {
        this.free3Value = free3Value;
    }

    public String getFree4() {
        return free4;
    }

    public void setFree4(final String free4) {
        this.free4 = free4;
    }

    public String getFree4Value() {
        return free4Value;
    }

    public void setFree4Value(final String free4Value) {
        this.free4Value = free4Value;
    }

    public String getFree5() {
        return free5;
    }

    public void setFree5(final String free5) {
        this.free5 = free5;
    }

    public String getFree5Value() {
        return free5Value;
    }

    public void setFree5Value(final String free5Value) {
        this.free5Value = free5Value;
    }

    public String getFree6() {
        return free6;
    }

    public void setFree6(final String free6) {
        this.free6 = free6;
    }

    public String getFree6Value() {
        return free6Value;
    }

    public void setFree6Value(final String free6Value) {
        this.free6Value = free6Value;
    }

    public String getFree7() {
        return free7;
    }

    public void setFree7(final String free7) {
        this.free7 = free7;
    }

    public String getFree7Value() {
        return free7Value;
    }

    public void setFree7Value(final String free7Value) {
        this.free7Value = free7Value;
    }

    public String getFree8() {
        return free8;
    }

    public void setFree8(final String free8) {
        this.free8 = free8;
    }

    public String getFree8Value() {
        return free8Value;
    }

    public void setFree8Value(final String free8Value) {
        this.free8Value = free8Value;
    }

    public String getFree9() {
        return free9;
    }

    public void setFree9(final String free9) {
        this.free9 = free9;
    }

    public String getFree9Value() {
        return free9Value;
    }

    public void setFree9Value(final String free9Value) {
        this.free9Value = free9Value;
    }

    public boolean isWorldWideAccess() {
        return worldWideAccess;
    }

    public void setWorldWideAccess(final boolean worldWideAccess) {
        this.worldWideAccess = worldWideAccess;
    }

    private boolean isEmpty(final String input) {

        if (input == null || input.trim().length() == 0) {
            return true;
        }
        return false;
    }

}
