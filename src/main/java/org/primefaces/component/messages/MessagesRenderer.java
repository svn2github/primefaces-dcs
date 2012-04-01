/*
 * Copyright 2009-2012 Prime Teknoloji.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.component.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.renderkit.CoreRenderer;

public class MessagesRenderer extends CoreRenderer {

    @Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException{
		Messages uiMessages = (Messages) component;
		ResponseWriter writer = context.getResponseWriter();
        String clientId = uiMessages.getClientId(context);		
		Map<String, List<FacesMessage>> messagesMap = new HashMap<String, List<FacesMessage>>();
        
        String _for = uiMessages.getFor();
        Iterator<FacesMessage> messages;
        if(_for != null) {
            messages = context.getMessages(_for);
        }
        else {
            messages = uiMessages.isGlobalOnly() ? context.getMessages(null) : context.getMessages();
        }
		
		while(messages.hasNext()) {
			FacesMessage message = messages.next();
			FacesMessage.Severity severity = message.getSeverity();
			
			if(message.isRendered() && !uiMessages.isRedisplay())
				continue;
			
			if(severity.equals(FacesMessage.SEVERITY_INFO)) addMessage(message, messagesMap, "info");
			else if(severity.equals(FacesMessage.SEVERITY_WARN))addMessage(message, messagesMap, "warn");
			else if(severity.equals(FacesMessage.SEVERITY_ERROR)) addMessage(message, messagesMap, "error");
			else if(severity.equals(FacesMessage.SEVERITY_FATAL)) addMessage(message, messagesMap, "fatal");	
		}
		
		writer.startElement("div", uiMessages);
		writer.writeAttribute("id", clientId, "id");
		writer.writeAttribute("class", "ui-messages ui-widget", null);
		
		for(String severity : messagesMap.keySet()) {
			List<FacesMessage> severityMessages = messagesMap.get(severity);
			
			if(severityMessages.size() > 0)
				encodeSeverityMessages(context, uiMessages, severity, severityMessages);
		}
		
		writer.endElement("div");
	}
    
    protected void addMessage(FacesMessage message, Map<String, List<FacesMessage>> messagesMap, String severity) {
        List<FacesMessage> severityMessages = messagesMap.get(severity);
        
        if(severityMessages == null) {
            severityMessages = new ArrayList<FacesMessage>();
            messagesMap.put(severity, severityMessages);
        }
        
        severityMessages.add(message);
    }

	protected void encodeSeverityMessages(FacesContext context, Messages uiMessages, String severity, List<FacesMessage> messages) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String styleClassPrefix = "ui-messages-" + severity;
		
		writer.startElement("div", null);
		writer.writeAttribute("class", styleClassPrefix + " ui-corner-all", null);
		
		writer.startElement("span", null);
		writer.writeAttribute("class", styleClassPrefix + "-icon", null);
		writer.endElement("span");
		
		writer.startElement("ul", null);
		
		for(FacesMessage msg : messages) {
			writer.startElement("li", null);
			
			String summary = msg.getSummary() != null ? msg.getSummary() : "";
            String detail = msg.getDetail() != null ? msg.getDetail() : summary;
            
            if(uiMessages.isShowSummary()) {
	            writer.startElement("span", null);
	            writer.writeAttribute("class", styleClassPrefix + "-summary", null);
	            writer.writeText(summary, null);
	            writer.endElement("span");
            }
            
            if(uiMessages.isShowDetail()) {
            	writer.startElement("span", null);
            	writer.writeAttribute("class", styleClassPrefix + "-detail", null);
            	writer.writeText(detail, null);
            	writer.endElement("span");
            }
            
			writer.endElement("li");
			
			msg.rendered();
		}
		
		writer.endElement("ul");
		
		writer.endElement("div");
	}
}