<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<%@ page import="ch.dbs.form.*" %>
<%@ page import="ch.dbs.entity.*" %>
<%@ page import="java.util.*" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

 <head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
  <title><bean:message bundle="systemConfig" key="application.name"/> - Standortverwaltung</title>
  <link rel="stylesheet" type="text/css" href="jsp/import/styles.css" />
  

  
 </head>
 
<body>
<tiles:insert page="import/header.jsp" flush="true" />
 
<div class="content">
<logic:present name="userinfo" property="benutzer">
      <logic:notEqual name="userinfo" property="benutzer.rechte" value="1">
<table
  style="position:absolute; text-align:left; left:<bean:message key="submenupos.stock" />px; z-index:2;">
  <tr>
    <td id="submenu" nowrap="nowrap" title="<bean:message key="menu.export_explain" />"><a
      href="allstock.do?method=prepareExport&activemenu=stock"><bean:message key="menu.export" /></a></td>
    <td id="submenu" nowrap="nowrap"
      title="<bean:message key="menu.import_explain" />"><a
      href="stock.do?method=prepareImport&activemenu=stock"><bean:message key="menu.import" /></a></td>
    <td id="submenuactive" nowrap="nowrap"
      title="<bean:message key="menu.locations_explain" />"><a
      href="modplace.do?method=listStockplaces&activemenu=stock"><bean:message key="menu.locations" /></a></td>
  </tr>
</table>
</logic:notEqual>
</logic:present>
 <br>
<h3><bean:message key="stockplacesmodify.header" /></h3>

<p><bean:message key="stockplacesmodify.intro" /><br></p>

<logic:equal name="holdingform" property="mod" value="false">

<logic:present name="holdingform" property="standorte">

<logic:present name="holdingform" property="message">
  <bean:define id="translationKey" name="holdingform" property="message" type="java.lang.String"/>
  <div id="italic"><bean:message key="<%=translationKey%>" /></div>
  <p></p>
</logic:present>

  <table class="border">
  <logic:iterate id="st" name="holdingform" property="standorte">
    <tr>
      <td id="border"><a href="modplace.do?method=changeStockplace&stid=<bean:write name="st" property="id" />"><bean:message key="stockplacesmodify.change" /></a></td>
        <td id="border">       
         <bean:write name="st" property="inhalt" />
    </td>
    <td id="border"><a href="modplace.do?method=changeStockplace&stid=<bean:write name="st" property="id" />&del=true"><bean:message key="stockplacesmodify.delete" /></a></td>
    </tr>
   </logic:iterate>   
  </table>
</logic:present>
</logic:equal>

<logic:equal name="holdingform" property="mod" value="true">
<html:form action="modplace.do" method="post" focus="standortid">
<table>
  <logic:iterate id="st" name="holdingform" property="standorte">
    <tr>
        <td>       
         <input name="standortid" type="text" size="50" maxlength="100" value="<bean:write name="st" property="inhalt" />" />
    </td>
    </tr>
   <input name="stid" type="hidden" value="<bean:write name="st" property="id" />" /> 
   </logic:iterate>
   <tr>
      <td><br></td>
    </tr>
   <tr>
      <td><br></td>
    </tr>
    <tr>
        <td><input type="submit" value="<bean:message key="stockplacesmodify.change" />"></td>
    </tr>
</table>
<input name="mod" type="hidden" value="true" />
<input name="method" type="hidden" value="changeStockplace" />
</html:form>      
</logic:equal>
              
</div>
</body>
</html>
