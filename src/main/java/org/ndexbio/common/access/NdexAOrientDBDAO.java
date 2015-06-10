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
package org.ndexbio.common.access;

import org.ndexbio.model.exceptions.NdexException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class NdexAOrientDBDAO {
	
    protected ODatabaseDocumentTx _ndexDatabase = null;
    
    @Deprecated    
    protected void setup() throws NdexException
    {
       _ndexDatabase = NdexDatabase.getInstance().getAConnection();
    }
    
    protected void teardown()
    {
        
        if (_ndexDatabase != null)
        {
            _ndexDatabase.close();
            _ndexDatabase = null;
        }
        
    }

}
