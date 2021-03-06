<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>


<table id="header_table">
  <tr style="background-image:url(img/verlauf.png); background-repeat:repeat-x; ">
    <td class="logo"><a href="<bean:message bundle="systemConfig" key="server.welcomepage"/>"><img class="logo" src='img/sp.gif' alt='<bean:message bundle="systemConfig" key="application.name"/>'  /></a></td>
    <td><h1><bean:message bundle="systemConfig" key="application.name"/></h1>
    <p></p>
    </td>
    <td class="kontoinfos"><tiles:insert page="kontoinfo.jsp" flush="true" /></td>
      <td width="20%" style="text-align:right;">
      <logic:present name="userinfo" property="benutzer">
      <a href="logout.do?activemenu=login"><font color="white"> [Log out]</font></a>
        </logic:present>
        <logic:notPresent name="userinfo" property="benutzer">
          <logic:present name="orderform" property="resolver">
            <logic:equal name="orderform" property="resolver" value="true">
              <nobr><a href="pl.do?<logic:present name="orderform">mediatype=<bean:write name="orderform" property="mediatype" />&issn=<bean:write name="orderform" property="issn" />&jahr=<bean:write name="orderform" property="jahr" />&jahrgang=<bean:write name="orderform" property="jahrgang" />&heft=<bean:write name="orderform" property="heft" />&seiten=<bean:write name="orderform" property="seiten" />&isbn=<bean:write name="orderform" property="isbn" />&artikeltitel=<bean:write name="orderform" property="artikeltitel_encodedUTF8" />&zeitschriftentitel=<bean:write name="orderform" property="zeitschriftentitel_encodedUTF8" />&author=<bean:write name="orderform" property="author_encodedUTF8" />&kapitel=<bean:write name="orderform" property="kapitel_encodedUTF8" />&buchtitel=<bean:write name="orderform" property="buchtitel_encodedUTF8" />&verlag=<bean:write name="orderform" property="verlag_encodedUTF8" />&rfr_id=<bean:write name="orderform" property="rfr_id" />&genre=<bean:write name="orderform" property="genre" />&pmid=<bean:write name="orderform" property="pmid" />&doi=<bean:write name="orderform" property="doi" />&sici=<bean:write name="orderform" property="sici" />&zdbid=<bean:write name="orderform" property="zdbid" />&lccn=<bean:write name="orderform" property="lccn" /></logic:present>">[Login]</a></nobr>
            </logic:equal>
            <logic:equal name="orderform" property="resolver" value="false">
              <a href="login.do?activemenu=login"><font color="white"> [Login]</font></a>
            </logic:equal>
          </logic:present>
          <logic:notPresent name="orderform" property="resolver">
            <a href="login.do?activemenu=login"><font color="white"> [Login]</font></a>
        </logic:notPresent>
        </logic:notPresent>
        </td>
        <td>&nbsp;</td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td colspan="3">
      <tiles:insert page="tabmenu.jsp" flush="true" />
    </td>
    <td>&nbsp;</td>
  </tr>
</table>






