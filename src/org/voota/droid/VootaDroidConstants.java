/*
 * This file is part of the Voota package.
 * (c) 2010 Tatyana Ulyanova <levkatata.voota@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * This file contains implementation of VootaDroidConstants class. This class 
 * includes general internal application constants. 
 *
 * @package    Voota
 * @subpackage Droid
 * @author     Tatyana Ulyanova
 * @version    1.0
 */

package org.voota.droid;

import org.voota.api.VootaApiException;

import android.content.Context;

public class VootaDroidConstants
{
    static final String BUNDLEKEY_ENTITYINFO;
    static final String BUNDLEKEY_ISPOLITIC;
    static final String BUNDLEKEY_SEARCHSTRING;
    
    static final String PREFERENCES_FILE;
    static final String PREFKEY_ACCESSTOKEN;
    static final String PREFKEY_TOKENSECRET;
    
    static final boolean ISPRODUCTION_BUILD;
    static final String HOSTNAME_TEST;
    
    static 
    {
        BUNDLEKEY_ENTITYINFO = "EntityInfo";
        BUNDLEKEY_ISPOLITIC = "IsPolitic";
        BUNDLEKEY_SEARCHSTRING = "SearchString";
        
        PREFERENCES_FILE = "VootaPreferences";
        PREFKEY_ACCESSTOKEN = "AccessToken";
        PREFKEY_TOKENSECRET = "TokenSecret";
        
        ISPRODUCTION_BUILD = true;
        HOSTNAME_TEST = "http://api.voota.org"; /*"http://dummy.voota.es";*/
    }
    
    static String getErrorMessage(int nErrorCode, Context context)
    {
        int nResID = R.string.errormsg_no_respond;
        
        switch(nErrorCode)
        {
        case VootaApiException.kErrorNoRespond:
            nResID = R.string.errormsg_no_respond;
            break;
        case VootaApiException.kErrorNoAuthorize:
            nResID = R.string.errormsg_no_authorize;
            break;
        case VootaApiException.kErrorReviewNotPosted:
            nResID = R.string.errormsg_review_not_posted;
            break;
        case VootaApiException.kErrorYouCantPostReview:
            nResID = R.string.errormsg_can_not_post_review;
            break;
        }

        return context.getString(nResID);
    }
}