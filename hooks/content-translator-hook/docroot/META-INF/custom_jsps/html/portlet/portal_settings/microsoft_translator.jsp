<%--
/*
* Copyright (C) 2005-2015 Rivet Logic Corporation.
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; version 2
* of the License.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor,
* Boston, MA 02110-1301, USA.
*/
--%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ page import="com.liferay.portal.kernel.util.PrefsPropsUtil" %>
<%@ page import="com.liferay.portal.kernel.util.PropsKeys" %>
<%@ page import="com.liferay.portal.kernel.util.PropsUtil" %>
<%@ page import="com.liferay.portal.kernel.util.GetterUtil" %>
<%@ page import="com.liferay.portal.util.PortletKeys" %>

<liferay-theme:defineObjects />

<% 
final String TRANSLATOR_CLIENT_ID = "microsoft.translator.clientid";
final String TRANSLATOR_CLIENTE_SECRET = "microsoft.translator.clientsecret";

String clientId = PrefsPropsUtil.getString(company.getCompanyId(), TRANSLATOR_CLIENT_ID, GetterUtil.getString(PropsUtil.get(TRANSLATOR_CLIENT_ID)));
String clientSecret = PrefsPropsUtil.getString(company.getCompanyId(), TRANSLATOR_CLIENTE_SECRET, GetterUtil.getString(PropsUtil.get(TRANSLATOR_CLIENTE_SECRET)));
%>

<h3><liferay-ui:message key="translator-auth" /></h3>
<p><liferay-ui:message key="translator-description" /><p>
<aui:fieldset>
	<aui:input required="true" label="translator-client-id" 
		name='<%= "settings--" + TRANSLATOR_CLIENT_ID + "--" %>' 
		type="text" value="<%= clientId %>" />

	<aui:input required="true" 		label="translator-client-secret" 
		name='<%= "settings--" + TRANSLATOR_CLIENTE_SECRET + "--" %>' 
		type="text" value="<%= clientSecret %>" />
</aui:fieldset>