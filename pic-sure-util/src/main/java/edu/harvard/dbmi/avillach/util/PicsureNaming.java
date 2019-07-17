package edu.harvard.dbmi.avillach.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PicsureNaming {

    public static class ExceptionMessages {
        /**
         * This class is used to store message text for various exceptions that are thrown
         *
         */

        // NotAuthorized Exception Messages
        public static final String MISSING_CREDENTIALS = "Missing credentials";

        // Application Exception Messages
        public final static String MISSING_TARGET_URL = "Resource is missing target URL";
        public final static String MISSING_RESOURCE_PATH = "Resource is missing resourceRS path";
        public final static String MISSING_RESOURCE = "Query is missing Resource";

        // Protocol Exception Messages
        public final static String MISSING_RESOURCE_ID = "Missing resource id";
        public final static String RESOURCE_NOT_FOUND = "No resource with id: ";
        public final static String MISSING_DATA = "Missing query request data";
        public final static String MISSING_QUERY_ID = "Missing query id";
        public final static String QUERY_NOT_FOUND = "No query with id: ";
        public static final String INCORRECTLY_FORMATTED_REQUEST = "Incorrectly formatted query request data";

        // General Exception Messages
        public final static String INTERNAL_SYSTEM_ERROR = "An internal system error has occured. Please contact your system administrator so they can view the error in the server logs.";
    }


    public static class RoleNaming{
        /**
         * please NOTICE:
         * This ROLE_SYSTEM is used across different projects
         * if this ROLE_SYSTEM naming changed, it will affect other projects, like picsure-micro-auth-app,
         * the suggestion is, if you want to change the naming of role_system,
         * please either create a new role named PIC_SURE_SYSTEM_ADMIN
         * or not use this naming in other project (this naming is not designed to use in other projects originally)
         */
        public static final String ROLE_SYSTEM = "ROLE_SYSTEM";
        public static final String ROLE_RESEARCH = "ROLE_RESEARCH";
        public static final String ROLE_USER = "ROLE_USER";
        public static final String ROLE_TOKEN_INTROSPECTION = "ROLE_TOKEN_INTROSPECTION";
        public static final String ROLE_INTROSPECTION_USER = "ROLE_INTROSPECTION_USER";


        public static List<String> allRoles(){
            List<String> roles = new ArrayList<>();
            for (Field field : RoleNaming.class.getFields()){
                roles.add(field.getName());
            }
            return roles;
        }
    }
}
