package com.example.client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "rechnungen")
public class Rechnungen {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "rechnung")
    private List<Rechnung> rechnungen;

    public List<Rechnung> getRechnungen() {
        return rechnungen;
    }
}
