package org.voota.droid;

public class VootaDroidConstants
{
    static final String BUNDLEKEY_ENTITYINFO;
    static final String BUNDLEKEY_ISPOLITIC;
    static final String BUNDLEKEY_SEARCHSTRING;
    
    static final String PREFERENCES_FILE;
    static final String PREFKEY_ACCESSTOKEN;
    static final String PREFKEY_TOKENSECRET;
    
    static final boolean ISPRODUCTION_BUILD;
    
    static 
    {
        BUNDLEKEY_ENTITYINFO = "EntityInfo";
        BUNDLEKEY_ISPOLITIC = "IsPolitic";
        BUNDLEKEY_SEARCHSTRING = "SearchString";
        
        PREFERENCES_FILE = "VootaPreferences";
        PREFKEY_ACCESSTOKEN = "AccessToken";
        PREFKEY_TOKENSECRET = "TokenSecret";
        
        ISPRODUCTION_BUILD = true;
    }
}