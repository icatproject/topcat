package org.icatproject.topcat.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TParameterType {
    private Long id;
    private String name;
    private String description;
    private String units;
    private String unitsFullName;
    private TParameterValueType valueType;

    public TParameterType() {
    }

    public TParameterType(Long id, String name, String description,
            String units, String unitsFullName, TParameterValueType valueType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.units = units;
        this.unitsFullName = unitsFullName;
        this.valueType = valueType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getUnitsFullName() {
        return unitsFullName;
    }

    public void setUnitsFullName(String unitsFullName) {
        this.unitsFullName = unitsFullName;
    }

    public TParameterValueType getValueType() {
        return valueType;
    }

    public void setValueType(TParameterValueType valueType) {
        this.valueType = valueType;
    }

}

