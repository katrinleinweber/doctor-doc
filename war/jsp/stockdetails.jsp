<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ page import="ch.dbs.form.*" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" lang="en_US" xml:lang="en_US">

 <head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
  <title><bean:message key="availresult.details" /></title>
 </head>
 <body>

<h3><bean:message key="availresult.library" /></h3>

<table border="0">
	<tr>
		<td>
			<bean:write name="konto" property="bibliotheksname" />
		</td>
	</tr>
	<tr>
		<td>
			<bean:write name="konto" property="adresse" />
		</td>
	</tr>
	<logic:notEmpty name="konto" property="adressenzusatz">
	<tr>
		<td>
			<bean:write name="konto" property="adressenzusatz" />
		</td>
	</tr>
	</logic:notEmpty>
	<tr>
		<td>
			<bean:write name="konto" property="land" />-<bean:write name="konto" property="PLZ" />&nbsp;<bean:write name="konto" property="ort" />
		</td>
	</tr>
	<tr>
		<td>
			<a href="mailto:<bean:write name="konto" property="bibliotheksmail" />"><bean:write name="konto" property="dbsmail" /></a>
		</td>
	</tr>

</table>

<logic:notEmpty name="orderform" property="issn">
<!-- Show only when we have an ISSN -->
<h3><bean:message key="issnsearch.titel" /></h3>

  <logic:notEmpty name="orderform" property="author"><bean:write name="orderform" property="author" /> : </logic:notEmpty>
  <logic:notEmpty name="orderform" property="artikeltitel"><bean:write name="orderform" property="artikeltitel" />. - </logic:notEmpty>
  <logic:notEmpty name="orderform" property="zeitschriftentitel"><bean:write name="orderform" property="zeitschriftentitel" /></logic:notEmpty><br />
  <logic:notEmpty name="orderform" property="jahr"><bean:write name="orderform" property="jahr" />;</logic:notEmpty><logic:notEmpty name="orderform" property="jahrgang"><bean:write name="orderform" property="jahrgang" /></logic:notEmpty><logic:notEmpty name="orderform" property="heft">(<bean:write name="orderform" property="heft" />)</logic:notEmpty><logic:notEmpty name="orderform" property="seiten">:<bean:write name="orderform" property="seiten" /></logic:notEmpty>
  <logic:notEmpty name="orderform" property="issn">. - ISSN: <bean:write name="orderform" property="issn" /></logic:notEmpty>
  <logic:notEmpty name="orderform" property="pmid">. - PMID: <a href="http://www.ncbi.nlm.nih.gov/pubmed/<bean:write name="orderform" property="pmid" />" target="_blank"><bean:write name="orderform" property="pmid" /></a></logic:notEmpty>
</logic:notEmpty>

<h3><bean:message key="availresult.link_title_print" /></h3>
<table>
	
	<logic:iterate id="stock" name="holdings">
	
	<tr>
		<td>
			<bean:message key="bestellform.zeitschrift" />:&nbsp;
		</td>
		<td>
    		<bean:write name="stock" property="holding.titel" />
    	</td>
    </tr>
	<tr>
		<td>
			<bean:message key="availresult.titel" />:&nbsp;
		</td>
		<td>
    		<logic:notEmpty name="stock" property="startyear"><bean:write name="stock" property="startyear" />;</logic:notEmpty><logic:notEmpty name="stock" property="startvolume"><bean:write name="stock" property="startvolume" /></logic:notEmpty><logic:notEmpty name="stock" property="startissue">(<bean:write name="stock" property="startissue" />)</logic:notEmpty> -
    		<logic:notEmpty name="stock" property="endyear"><bean:write name="stock" property="endyear" />;</logic:notEmpty><logic:notEmpty name="stock" property="endvolume"><bean:write name="stock" property="endvolume" /></logic:notEmpty><logic:notEmpty name="stock" property="endissue">(<bean:write name="stock" property="endissue" />)</logic:notEmpty> 
    	</td>
    </tr>
    <logic:notEmpty name="stock" property="bemerkungen">
    <tr>
		<td>
			<bean:message key="bestellform.bemerkungen" />:&nbsp;
		</td>
		<td>
    		<bean:write name="stock" property="bemerkungen" />
    	</td>
    </tr>
    </logic:notEmpty>
    <tr>
		<td>
			<bean:message key="stockimport.suppl" />:&nbsp;
		</td>
		<td>
    		<logic:equal name="stock" property="suppl" value="0"><bean:message key="stockimport.suppl_0" /></logic:equal>
    		<logic:equal name="stock" property="suppl" value="1"><bean:message key="stockimport.suppl_1" /></logic:equal>
    		<logic:equal name="stock" property="suppl" value="2"><bean:message key="stockimport.suppl_2" /></logic:equal>
    	</td>
    </tr>
    <tr>
		<td>
			ISSN:&nbsp;
		</td>
		<td>
    		<bean:write name="stock" property="holding.issn" />
    	</td>
    </tr>
    <logic:notEmpty name="stock" property="holding.verlag">
    <tr>
		<td>
			<bean:message key="bestellform.verlag" />:&nbsp;
		</td>
		<td>
    		<logic:notEmpty name="stock" property="holding.ort"><bean:write name="stock" property="holding.ort" /> : </logic:notEmpty><bean:write name="stock" property="holding.verlag" />
    	</td>
    </tr>
    </logic:notEmpty>
    <tr>
		<td>
			<bean:message key="stockimport.loc" />:&nbsp;
		</td>
		<td>
    		<bean:write name="stock" property="standort.inhalt" />
    	</td>
    </tr>
    <logic:notEmpty name="stock" property="shelfmark">
    <tr>
		<td>
			<bean:message key="stockimport.sig" />:&nbsp;
		</td>
		<td>
    		<bean:write name="stock" property="shelfmark" />
    	</td>
    </tr>
    </logic:notEmpty>
    <tr><td><br /></td></tr>
    
     </logic:iterate>
     	
</table>

<tiles:insert page="import/footer.jsp" flush="true" />
 
 </body>
</html>