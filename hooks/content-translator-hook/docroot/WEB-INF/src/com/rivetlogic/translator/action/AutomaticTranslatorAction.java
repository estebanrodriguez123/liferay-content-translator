package com.rivetlogic.translator.action;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.struts.StrutsPortletAction;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.storage.Field;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;
import com.liferay.portlet.dynamicdatamapping.util.DDMUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalArticleServiceUtil;
import com.liferay.portlet.journal.util.JournalConverterUtil;
import com.rivetlogic.translator.api.Languages;
import com.rivetlogic.translator.api.TranslatorAPI;
import com.rivetlogic.translator.api.TranslatorException;
import com.rivetlogic.translator.util.AutomaticTranslatorUtil;
import com.rivetlogic.translator.util.WebKeys;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class AutomaticTranslatorAction extends BaseStrutsPortletAction {

    private static final Log LOG = LogFactoryUtil.getLog(AutomaticTranslatorAction.class);
    
    public void processAction(
            StrutsPortletAction originalStrutsPortletAction,
            PortletConfig portletConfig, ActionRequest actionRequest,
            ActionResponse actionResponse)
        throws Exception {

        UploadPortletRequest uploadPortletRequest =
                PortalUtil.getUploadPortletRequest(actionRequest);
        LOG.debug("Translate article " + MapUtil.toString(uploadPortletRequest.getParameterMap()));
        
        
        String redirect = ParamUtil.getString(actionRequest, "redirect");
        String portletId = HttpUtil.getParameter(redirect, "p_p_id", false);
        String namespace = PortalUtil.getPortletNamespace(portletId);
        ServiceContext serviceContext = ServiceContextFactory.getInstance(
                JournalArticle.class.getName(), uploadPortletRequest);
        
        String articleId = ParamUtil.getString(actionRequest, "articleId");
        long groupId = ParamUtil.getLong(actionRequest, "groupId");
        double version = ParamUtil.getDouble(uploadPortletRequest, "version");
        String[] toLanguageIds = ParamUtil.getParameterValues(uploadPortletRequest, "selectedLanguagesCheckbox");
        
        JournalArticle article = JournalArticleServiceUtil.getArticle(
                groupId, articleId, version);
        String structureId = ParamUtil.getString(uploadPortletRequest, "structureId");
        DDMStructure ddmStructure = null;
        
        String defaultLanguageId = article.getDefaultLanguageId();
        
        Locale defaultLocale = LocaleUtil.fromLanguageId(defaultLanguageId);
        String content = article.getContentByLocale(defaultLanguageId);
        String title = article.getTitle(defaultLocale);
        String description = article.getDescription(defaultLocale);
        LOG.debug("Title: " + title);
        LOG.debug("Description: " + description);
        LOG.debug("Content: " + content);
        
        Map<String, byte[]> images = new HashMap<String, byte[]>();
        TranslatorAPI translateUtil = AutomaticTranslatorUtil.getTranslateAPI(actionRequest);
        try {
            for(String languageId : toLanguageIds) {
                Locale locale = LocaleUtil.fromLanguageId(languageId);
                LOG.debug("Version: " + version);
                
                LOG.debug("Translate: " + title + " from: " + defaultLocale.getLanguage() + " to: " + locale.getLanguage());
                String newTitle = translateUtil.translate(title, defaultLocale.getLanguage(), locale.getLanguage());
                LOG.debug("Result: " + newTitle);
                
                LOG.debug("Translate: " + description + " from: " + defaultLocale.getLanguage() + " to: " + locale.getLanguage());
                String newDescription = translateUtil.translate(description, defaultLocale.getLanguage(), locale.getLanguage());
                LOG.debug("Result: " + newDescription);
                
                if (Validator.isNull(structureId)) {
                    LOG.debug("structureId is null");
                    if (!article.isTemplateDriven()) {
                        String curContent = StringPool.BLANK;
                        
                        Document document = SAXReaderUtil.read(content);
    
                        Element rootElement = document.getRootElement();
                        List<Element> staticContentElements = rootElement.elements(
                                "static-content");
                        
                        Element staticContentElement = staticContentElements.get(0);
                        curContent = article.getContent();
                        LOG.debug("Translate: " + staticContentElement.getText() + " from: " + defaultLocale.getLanguage() + " to: " + locale.getLanguage());
                        String newContent = translateUtil.translate(staticContentElement.getText(), defaultLocale.getLanguage(), locale.getLanguage());
                        LOG.debug("Result: " + newContent);
                        
                        content = LocalizationUtil.updateLocalization(
                                curContent, "static-content", newContent, languageId,
                                defaultLanguageId, true, true);
                        LOG.debug("New content: " + content);
    
                    }
                } else {
                    ddmStructure = DDMStructureLocalServiceUtil.getStructure(
                            PortalUtil.getSiteGroupId(groupId),
                            PortalUtil.getClassNameId(JournalArticle.class), structureId,
                            true);
                
                    if (article.isTemplateDriven()) {
                        Fields newFields = DDMUtil.getFields(
                            ddmStructure.getStructureId(), serviceContext);
        
                        Fields existingFields = JournalConverterUtil.getDDMFields(
                            ddmStructure, article.getContent());
        
                        Fields mergedFields = DDMUtil.mergeFields(
                            newFields, existingFields);
        
                        content = JournalConverterUtil.getContent(
                            ddmStructure, mergedFields);
                        
                        LOG.debug("Old Content: " + content);
                        
                        for(Field field : mergedFields) {
                            LOG.debug(field.getValue(defaultLocale));
                            if(field.getType().contains("text")) {
                                String currValue = String.valueOf(field.getValue(defaultLocale));
                                LOG.debug("Field Value: " + currValue);
                                String newValue = translateUtil.translate(currValue, defaultLocale.getLanguage(), locale.getLanguage());
                                field.setValue(locale, newValue);
                                LOG.debug("Field new Value: " + newValue);
                            }
                        }
                        
                        content = JournalConverterUtil.getContent(
                                ddmStructure, mergedFields);
                        LOG.debug("New Content: " + content);
                        
                    }
                }
                
                article = JournalArticleServiceUtil.updateArticleTranslation(
                        groupId, articleId, version, locale, newTitle, newDescription,
                        content, images, serviceContext);
                serviceContext.setFormDate(new Date());
                version = article.getVersion();
                LOG.debug("New version: " + version);
            }
            
            actionResponse.setRenderParameter(WebKeys.TRANSLATOR_CLOSE, "true");
        } catch(TranslatorException e) {
            SessionErrors.add(actionRequest, WebKeys.TRANSLATOR_ERROR_KEY);
            LOG.error(e.getMessage());
        }
        
    }
    
    public String render(
            StrutsPortletAction originalStrutsPortletAction,
            PortletConfig portletConfig, RenderRequest renderRequest,
            RenderResponse renderResponse)
        throws Exception {

        String articleId = ParamUtil.getString(renderRequest, "articleId");
        long groupId = ParamUtil.getLong(renderRequest, "groupId");
        int status = ParamUtil.getInteger(renderRequest, "status", WorkflowConstants.STATUS_ANY);
        JournalArticle article = JournalArticleLocalServiceUtil.getLatestArticle(groupId, articleId, status);
        
        renderRequest.setAttribute(WebKeys.JOURNAL_ARTICLE, article);
        
        renderRequest.setAttribute(WebKeys.TRANSLATOR_DISABLE, false);
        // Should we get the available languages from the API each time the translator is rendered?
        renderRequest.setAttribute(WebKeys.TRANSLATOR_LANGUAGES, Languages.LANGUAGES);

        if(!AutomaticTranslatorUtil.validateCredentials(renderRequest)) {
            SessionErrors.add(renderRequest, WebKeys.TRANSLATOR_AUTH_ERROR);
            renderRequest.setAttribute(WebKeys.TRANSLATOR_DISABLE, true);
        }
        
        return "/portlet/journal/translate.jsp";

    }

}
