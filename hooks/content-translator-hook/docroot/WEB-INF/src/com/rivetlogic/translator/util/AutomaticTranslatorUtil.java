package com.rivetlogic.translator.util;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.theme.ThemeDisplay;
import com.rivetlogic.translator.api.TranslatorAPI;

import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;

/**
 * Utility class to get the credentials from the control panel.
 * @author joseross
 *
 */
public class AutomaticTranslatorUtil {

    private static final Log LOG = LogFactoryUtil.getLog(AutomaticTranslatorUtil.class);

    /**
     * Used to check if the values of the credentials in the control panel are valid.
     */
    public static boolean validateCredentials(RenderRequest request) throws SystemException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        String clientId = getClientId(themeDisplay.getCompanyId());
        String clientSecret = getClientSecret(themeDisplay.getCompanyId());
        return !clientId.equals(StringPool.BLANK) && !clientId.equals("CLIENT_ID") &&
                !clientSecret.equals(StringPool.BLANK) && !clientSecret.equals("CLIENT_SECRET");
    }
    
    /**
     * Used to get an instance of TranslatorAPI, using the credentials from the control panel.
     */
    public static TranslatorAPI getTranslateAPI(ActionRequest request) throws SystemException {
        LOG.debug("Getting preferences from control panel");
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        TranslatorAPI.setClientId(getClientId(themeDisplay.getCompanyId()));
        TranslatorAPI.setClientSecret(getClientSecret(themeDisplay.getCompanyId()));
        LOG.debug("CLIENT_ID: " + TranslatorAPI.getClientId());
        LOG.debug("CLIENT_SECRET: " + TranslatorAPI.getClientSecret());
        return new TranslatorAPI();
    }
    
    /**
     * Get the value for ClientId from the control panel
     */
    private static String getClientId(long companyId) throws SystemException {
        return PrefsPropsUtil.getString(companyId, WebKeys.TRANSLATOR_CLIENT_ID, 
                GetterUtil.getString(PropsUtil.get(WebKeys.TRANSLATOR_CLIENT_ID)));
    }
    
    /**
     * Get the value for ClientSecret from the control panel
     */
    private static String getClientSecret(long companyId) throws SystemException {
        return PrefsPropsUtil.getString(companyId, WebKeys.TRANSLATOR_CLIENT_SECRET, 
                GetterUtil.getString(PropsUtil.get(WebKeys.TRANSLATOR_CLIENT_SECRET)));
    }
    
}
