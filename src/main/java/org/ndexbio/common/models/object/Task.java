package org.ndexbio.common.models.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ndexbio.common.models.data.ITask;
import org.ndexbio.common.models.data.Priority;
import org.ndexbio.common.models.data.Status;
import org.ndexbio.common.models.data.TaskType;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Task extends NdexObject
{
    private String _description;
    private Priority _priority;
    private int _progress;
    private String _resource;
    private Status _status;
    private TaskType _type;

    
    
    /**************************************************************************
    * Default constructor.
    **************************************************************************/
    public Task()
    {
        super();
    }
    
    /**************************************************************************
    * Populates the class (from the database) and removes circular references.
    * 
    * @param task The Task with source data.
    **************************************************************************/
    public Task(ITask task)
    {
        super(task);
        
        this.setCreatedDate(task.getStartTime());
        
        _description = task.getDescription();
        _priority = task.getPriority();
        _progress = task.getProgress();
        _resource = task.getResource();
        _status = task.getStatus();
        _type = task.getType();
    }


    
    public String getDescription()
    {
        return _description;
    }
    
    public void setDescription(String description)
    {
        _description = description;
    }
    
    public Priority getPriority()
    {
        return _priority;
    }
    
    public void setPriority(Priority priority)
    {
        _priority = priority;
    }
    
    public int getProgress()
    {
        return _progress;
    }
    
    public void setProgress(int progress)
    {
        _progress = progress;
    }
    
    public String getResource()
    {
        return _resource;
    }
    
    public void setResource(String resource)
    {
        _resource = resource;
    }

    public Status getStatus()
    {
        return _status;
    }

    public void setStatus(Status status)
    {
        _status = status;
    }
    
    public TaskType getType()
    {
        return _type;
    }
    
    public void setType(TaskType type)
    {
        _type = type;
    }
}