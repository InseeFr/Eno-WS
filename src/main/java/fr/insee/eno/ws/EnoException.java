package fr.insee.eno.ws;

import java.io.IOException;


public class EnoException extends IOException {

    private int status;
    private String details;

    /**
     *
     * @param status
     * @param message
     * @param details
     */
    public EnoException(int status, String message, String details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    public RestMessage toRestMessage(){
        return new RestMessage(this.status, this.getMessage(), this.details);
    }
}
