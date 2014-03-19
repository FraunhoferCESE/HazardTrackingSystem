package org.fraunhofer.plugins.hts.rest;

import javax.xml.bind.annotation.*;
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class htsRestModel {

    @XmlElement(name = "value")
    private String message;

    public htsRestModel() {
    }

    public htsRestModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}