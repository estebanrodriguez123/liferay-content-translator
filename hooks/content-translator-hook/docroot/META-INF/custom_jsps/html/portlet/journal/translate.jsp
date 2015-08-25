<%@page import="com.liferay.portal.kernel.util.HtmlUtil"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>

<%@page import="java.util.Arrays" %>
<%@page import="java.util.List" %>

<%@ include file="/html/portlet/journal/init.jsp" %>

<%
JournalArticle article = (JournalArticle)request.getAttribute(WebKeys.JOURNAL_ARTICLE);
String articleId = BeanParamUtil.getString(article, request, "articleId");
String structureId = BeanParamUtil.getString(article, request, "structureId");
String redirect = ParamUtil.getString(request, "redirect");
long groupId = BeanParamUtil.getLong(article, request, "groupId", scopeGroupId);
int status = BeanParamUtil.getInteger(article, request, "status");
double version = BeanParamUtil.getDouble(article, request, "version", JournalArticleConstants.VERSION_DEFAULT);
pageContext.setAttribute("pns", renderResponse.getNamespace());

boolean close = ParamUtil.getBoolean(request, "translatorClose");
boolean disable = (Boolean) request.getAttribute("translatorDisable");
String[] availableTranslations = (String[]) request.getAttribute("translatorLanguages");

%>

<portlet:actionURL var="translateArticleActionURL" windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="struts_action" value="/journal/translate" />
    <portlet:param name="redirect" value="<%= redirect %>" />
</portlet:actionURL>

<liferay-ui:error key="translator-error" message="translator-error-message"/>
<liferay-ui:error key="translator-auth-error" message="translator-auth-error-message"/>

<aui:form action="<%= translateArticleActionURL %>" cssClass="lfr-dynamic-form" enctype="multipart/form-data" method="post" name="fm1">
    <aui:input name="articleId" type="hidden" value="<%= articleId %>" />
    <aui:input name="groupId" type="hidden" value="<%= groupId %>" />
    <aui:input name="version" type="hidden" value="<%= ((article == null) || article.isNew()) ? version : article.getVersion() %>" />
    <aui:input name="status" type="hidden" value="<%= status %>" />
    <aui:input name="structureId" type="hidden" value="<%= structureId %>" />
    
    <aui:field-wrapper name="selectedLanguages" label="journal.article.form.translate.select.language">
    <%
    Locale[] locales = LanguageUtil.getAvailableLocales(themeDisplay.getSiteGroupId());
	
    for (int i = 0; i < locales.length; i++) {
        if (!ArrayUtil.contains(article.getAvailableLanguageIds(), LocaleUtil.toLanguageId(locales[i])) &&
                ArrayUtil.contains(availableTranslations, LocaleUtil.toLanguageId(locales[i]).split("_")[0])) {
            
    %>
        <div>
	        <aui:input name="selectedLanguages" type="checkbox" inlineLabel="right" label="" value="<%=LocaleUtil.toLanguageId(locales[i]) %>"/>
	        <liferay-ui:icon
	            image='<%= "../language/" + LocaleUtil.toLanguageId(locales[i]) %>'
	            message="<%= locales[i].getDisplayName(locale) %>"
	        />
	        <span class="lfr-icon-menu-text"><%= locales[i].getDisplayName(locale) %></span>
        </div>
    <%
        }
    }
    %>
    </aui:field-wrapper>
    
    <aui:button name="saveButton" type="submit" value="continue" disabled="<%= disable %>" />
    <aui:button href="<%= redirect %>" type="cancel" />
</aui:form>

<% if(close) {  %>
<aui:script use="aui-base">
	Liferay.fire('closeWindow',
	{
		id: '<portlet:namespace/>automatic-translate',
		redirect: '<%= HtmlUtil.escapeJS(redirect) %>'
	});
</aui:script>
<% } %>