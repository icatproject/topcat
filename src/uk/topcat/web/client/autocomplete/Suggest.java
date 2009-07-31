package uk.topcat.web.client.autocomplete;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

public class Suggest {
	
	/**
     * To make this return a DTO that allows you to grab multiple values, see
     * the following tutorial:
     * <p/>
     * http://eggsylife.blogspot.com/2008/08/gwt-suggestbox-backed-by-dto-model.html
     *
     * @return names of possible contacts
     */
    public static MultiWordSuggestOracle getSuggestions() {
        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
        oracle.add("gold");
        oracle.add("lead");
        oracle.add("He");
        oracle.add("Powder Diffraction");
        oracle.add("Neutron Flux");
        oracle.add("Neutron");
        oracle.add("Muon");
        oracle.add("X-ray");
        oracle.add("Polarisation");
        oracle.add("Spin Relaxation");
        oracle.add("Beam Guide");
        oracle.add("Methane Moderater");
        oracle.add("Neutrino");
        oracle.add("Plasma");
        oracle.add("Pressure");
        oracle.add("Wave Theory");
        oracle.add("Graphite");
        oracle.add("Magnetism");
        oracle.add("Elastic");
        oracle.add("Inelastic");
        oracle.add("MAPS");
        oracle.add("SXD");
        oracle.add("P45");
        
        return oracle;
    }

}
