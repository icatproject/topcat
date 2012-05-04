/**
 * 
 * Copyright (c) 2009-2012
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.gwt.client.manager;

/**
 * Imports
 */
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

/**
 * This class is a History manager that can process the history string and build
 * a history string upon interactions by the user.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class HistoryManager implements ValueChangeHandler<String> {
    TopcatWindowManager tcWindowManager;
    private String tabSelected = "";
    public static final String seperatorModel = "///";
    public static final String seperatorToken = "&";
    public static final String seperatorKeyValues = "=";
    public static final String logonError = "logonError";

    public HistoryManager() {
        // Add history handler
        History.addValueChangeHandler(this);
    }

    /**
     * Constructor.
     * 
     * @param tcWindowManager
     *            Floating Window Manager
     */
    public HistoryManager(TopcatWindowManager tcWindowManager) {
        this.tcWindowManager = tcWindowManager;
        // Add history handler
        History.addValueChangeHandler(this);
    }

    /**
     * @return the full current history string
     */
    public String getHistoryString() {
        String history = "view";
        history += seperatorModel + seperatorToken + "tab" + seperatorKeyValues + tabSelected;
        if (tcWindowManager != null) {
            history += tcWindowManager.getWindowHistoryString();
        }
        return history;
    }

    /**
     * Set the current selected tab.
     * 
     * @param tab
     */
    public void setTabSelected(String tab) {
        tabSelected = tab;
    }

    /*
     * This is History method, The format of the history is
     * /view//model=investigation
     * &Name=xyz&Id=123&ServerName=ISIS//model=dataset&
     * Name=default&Id=234&ServerName=ISIS (non-Javadoc)
     * 
     * @see
     * com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(
     * com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        String historyToken = event.getValue();
        processHistory(historyToken);
    }

    /**
     * Process the input history string.
     * 
     * @param history
     */
    public void processHistory(String history) {
        // First token will be special token or panel
        if (history.startsWith("view")) {
            processHistoryString(history);
            tcWindowManager.processHistoryString(history);
        }
    }

    /**
     * Process the input string to check for selected tab and floating window
     * history.
     * 
     * @param history
     */
    private void processHistoryString(String history) {
        String[] historyTokenList = history.split(HistoryManager.seperatorModel);
        // tab string
        String tabString = HistoryManager.seperatorToken + "tab";
        String logonErrorString = HistoryManager.seperatorToken + logonError;
        // split the models
        for (String hToken : historyTokenList) {
            if (hToken.startsWith(tabString) || hToken.startsWith(logonErrorString)) {
                // split the hToken to model params
                String[] paramList = hToken.split(HistoryManager.seperatorToken);
                for (String param : paramList) {
                    String[] keyvalues = param.split(HistoryManager.seperatorKeyValues);
                    try {
                        String key = keyvalues[0];
                        String value = keyvalues[1];
                        if (key.compareToIgnoreCase("tab") == 0) {
                            if (EventPipeLine.getInstance().getMainWindow() != null) {
                                EventPipeLine.getInstance().getMainWindow().getMainPanel()
                                        .selectPanelWithoutHistory(value);
                            }
                        } else if (key.compareToIgnoreCase(logonError) == 0) {
                            // remove error message
                            String[] newHistory = history.split(seperatorModel + seperatorToken + logonError);
                            History.newItem(newHistory[0]);
                            // prompt for logon and display error
                            EventPipeLine.getInstance().showLoginWidget(value);
                            EventPipeLine.getInstance().showErrorDialog(
                                    "ERROR logging on to " + value
                                            + " with credentials from external authorisation service");
                        }
                    } catch (IndexOutOfBoundsException ex) {
                    }
                }
            }
        }
    }

    /**
     * Removes the history string from the current history.
     * 
     * @param history
     */
    public void removeWindowHistory(String history) {
        if (history != null && history.compareToIgnoreCase("") != 0) {
            String currHistory = com.google.gwt.http.client.URL.decode(History.getToken());
            int index = currHistory.indexOf(history);
            String newHistory = "";
            if (index != -1) {
                newHistory = currHistory.substring(0, index);
                newHistory += currHistory.substring(index + history.length());
                History.newItem(newHistory);
            }
        }
    }

    /**
     * Creates a new history string.
     */
    public void updateHistory() {
        History.newItem(getHistoryString());
    }

}
