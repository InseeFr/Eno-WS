package fr.insee.eno.ws.controller.utils;

import java.io.File;

public class HeaderUtils {

    private HeaderUtils() {}

    public static String headersAttachment(File outputFile) {
        return "attachment;filename=\"" + outputFile.getName() + "\"";
    }

}
