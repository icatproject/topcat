/**
 * 
 * Copyright (c) 2009-2010
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
package uk.ac.stfc.topcat.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;

/**
 * @author sn65
 * 
 */
public class KeywordsSuggestOracle extends SuggestOracle {
	/**
	 * Topcat Session Id
	 */
	private String sessionId;

	/**
	 * Server to get the keywords from
	 */
	private String serverName;

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Class to hold a response from the server.
	 */
	private static class ServerResponse {

		/**
		 * Request made by the SuggestBox.
		 */
		private final Request request;

		/**
		 * The number of suggestions the server was asked for
		 */
		private final int serverSuggestionsLimit;

		/**
		 * Suggestions returned by the server in response to the request.
		 */
		private final ArrayList<Suggestion> suggestions;

		/**
		 * Create a new instance.
		 * 
		 * @param request
		 *            Request from the SuggestBox.
		 * @param serverSuggestionsLimit
		 *            The number of suggestions we asked the server for.
		 * @param suggestions
		 *            The suggestions returned by the server.
		 */
		private ServerResponse(Request request, int serverSuggestionsLimit,
				ArrayList<Suggestion> suggestions) {
			this.request = request;
			this.serverSuggestionsLimit = serverSuggestionsLimit;
			this.suggestions = suggestions;
		}

		/**
		 * Get the query string that was sent to the server.
		 * 
		 * @return The query.
		 */
		private String getQuery() {
			return request.getQuery();
		}

		/**
		 * Does the response include all possible suggestions for the query.
		 * 
		 * @return True or false.
		 */
		private boolean isComplete() {
			return suggestions.size() <= serverSuggestionsLimit;
		}

		/**
		 * Filter the suggestions we got back from the server.
		 * 
		 * @param query
		 *            The query string.
		 * @param limit
		 *            The number of suggestions to return.
		 * @return The suggestions.
		 */
		public List<Suggestion> filter(String query, int limit) {

			List<Suggestion> newSuggestions = new ArrayList<Suggestion>(limit);
			int i = 0, s = suggestions.size();
			while (i < s
					&& !suggestions.get(i).getDisplayString().toLowerCase()
							.startsWith(query.toLowerCase())) {
				++i;
			}
			while (i < s
					&& newSuggestions.size() < limit
					&& suggestions.get(i).getDisplayString().toLowerCase()
							.startsWith(query.toLowerCase())) {
				newSuggestions.add(suggestions.get(i));
				++i;
			}
			return newSuggestions;
		}
	}

	/* Whether any request is being processed */
	private boolean requestInProgress = false;
	/* most recent request that is being processed */
	private Request mostRecentClientRequest = null;
	/**
	 * The most recent response from the server.
	 */
	private ServerResponse mostRecentServerResponse = null;

	/* Search service */
	private final SearchServiceAsync searchServce = GWT
			.create(SearchService.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.user.client.ui.SuggestOracle#requestSuggestions(com.google
	 * .gwt.user.client.ui.SuggestOracle.Request,
	 * com.google.gwt.user.client.ui.SuggestOracle.Callback)
	 */
	@Override
	public void requestSuggestions(Request request, Callback callback) {
		// Record this request as the most recent one.
		mostRecentClientRequest = request;
		// Check whether there is a request already in processing,if already in
		// processing then it will return when the processing is
		// completed.
		if (!requestInProgress) {
			returnSuggestions(callback);
		}
	}

	private void returnSuggestions(Callback callback) {
		// For single character queries return an empty list.
		final String mostRecentQuery = mostRecentClientRequest.getQuery();
		if (mostRecentQuery.length() == 1
				|| mostRecentQuery.trim().length() == 1) {
			callback.onSuggestionsReady(mostRecentClientRequest, new Response(
					Collections.<Suggestion> emptyList()));
			return;
		}
		// If we have a response from the server, and it includes all the
		// possible suggestions for its request, and
		// that request is a superset of the request we're trying to satisfy now
		// then use the server results, otherwise
		// ask the server for some suggestions.
		if (mostRecentServerResponse != null) {
			if (mostRecentQuery.equals(mostRecentServerResponse.getQuery())) {
				Response resp = new Response(mostRecentServerResponse.filter(
						mostRecentClientRequest.getQuery(),
						mostRecentClientRequest.getLimit()));
				callback.onSuggestionsReady(mostRecentClientRequest, resp);
			} else if (mostRecentServerResponse.isComplete()
					&& mostRecentQuery.startsWith(mostRecentServerResponse
							.getQuery())) {
				Response resp = new Response(mostRecentServerResponse.filter(
						mostRecentClientRequest.getQuery(),
						mostRecentClientRequest.getLimit()));
				callback.onSuggestionsReady(mostRecentClientRequest, resp);
			} else {
				makeRequest(mostRecentClientRequest, callback);
			}
		} else {
			makeRequest(mostRecentClientRequest, callback);
		}
	}

	/**
	 * Send a request to the server.
	 * 
	 * @param request
	 *            The request.
	 * @param callback
	 *            The callback to call when the request returns.
	 */
	private void makeRequest(final Request request, final Callback callback) {
		requestInProgress = true;
		searchServce.getKeywordsFromServer(this.sessionId, serverName, request
				.getQuery(), 500, new AsyncCallback<List<String>>() {
			public void onFailure(Throwable caught) {
				requestInProgress = false;
			}

			@Override
			public void onSuccess(List<String> result) {
				// TODO Auto-generated method stub
				requestInProgress = false;
				ArrayList<Suggestion> suggestionList = new ArrayList<Suggestion>();
				for (int i = 0; i < result.size(); i++)
					suggestionList.add(new MultiWordSuggestion(result.get(i),
							result.get(i)));
				mostRecentServerResponse = new ServerResponse(request, 500,
						suggestionList);
				KeywordsSuggestOracle.this.returnSuggestions(callback);
			}
		});
	}
}
