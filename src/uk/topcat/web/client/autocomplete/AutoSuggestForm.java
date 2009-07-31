package uk.topcat.web.client.autocomplete;

import java.util.List;

import uk.topcat.web.client.autocomplete.InputListWidget;

import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SuggestBox;

public class AutoSuggestForm extends Composite {
    FlowPanel form;
    MultipleTextBox txt;
    SuggestBox box;
    InputListWidget ilw;
    
    public AutoSuggestForm() {
        form = new FlowPanel();
        form.setStyleName("form");
        initWidget(form);

        //form.add(new HTML("<p>Type in the box below to see basic autocomplete in action...</p>"));

        txt = new MultipleTextBox();
        box = new SuggestBox(Suggest.getSuggestions(), txt);
        box.addStyleName("original-token-input");
        box.setAnimationEnabled(true);

        //form.add(box);

        //form.add(new HTML("<p style='margin-top: 20px'>Type in the box below to see autocomplete with Facebook-style formatting.</p>"));

        // Facebook Style Autocompleter
        // CSS and DIV structure from http://loopj.com/tokeninput/demo.html:

        // 1. Create an input field
        ilw = new InputListWidget();
        form.add(ilw);

        //form.add(new HTML("<p>For more information about this demo, see <a href=\"http://raibledesigns.com/rd/entry/creating_a_facebook_style_autocomplete\">Creating a Facebook-style Autocomplete with GWT</a>."));
    }
    
    public List<String> getItemsSelected() {
    	return ilw.getItemsSelected();
    }

    public void onSubmit(DomEvent<EventHandler> event) {
        // no-op
    	
    }
}

