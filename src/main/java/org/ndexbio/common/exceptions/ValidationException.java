package org.ndexbio.common.exceptions;

import org.ndexbio.model.exceptions.NdexException;

public class ValidationException extends NdexException
{
    private static final long serialVersionUID = 1L;

    
    
    public ValidationException(String message)
    {
        super(message);
    }

    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
