/**
 *   Copyright (c) 2013, 2015
 *  	The Regents of the University of California
 *  	The Cytoscape Consortium
 *
 *   Permission to use, copy, modify, and distribute this software for any
 *   purpose with or without fee is hereby granted, provided that the above
 *   copyright notice and this permission notice appear in all copies.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.ndexbio.xgmml.parser.handler;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.xgmml.parser.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleViewEdgeGraphics extends AbstractHandler {

	@Override
    public ParseState handle(final String namespace, final String tag, final String qName,  Attributes atts, ParseState current) throws SAXException, NdexException {
        final Object edgeId = manager.getCurrentElementId();
		
        if (edgeId == null) {
        	logger.error("Cannot parse edge view: edge id is null");
        	return current;
        }
        
		if (tag.equals("graphics")) {
			throw new NdexException("view edge graphics tag not yet supported");
        	//manager.addViewGraphicsAttributes(edgeId, atts, false);
        } else if (tag.equals("att")) {
            String name = atts.getValue("name");

            if (AttributeValueUtil.LOCKED_VISUAL_PROPS.equalsIgnoreCase(name))
            	return ParseState.LOCKED_VISUAL_PROP_ATT;
            
            String value = atts.getValue(AttributeValueUtil.ATTR_VALUE);
            
            if (name != null && value != null)
            	throw new NdexException("view edge graphics tag not yet supported");
                //manager.addViewGraphicsAttribute(edgeId, name, value, false);
        }
        
        return current;
    }
}