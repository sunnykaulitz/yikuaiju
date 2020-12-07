package com.example.yikuaiju.bean.common;

import java.io.Serializable;

public class CommonBean implements Serializable{
	private static final long serialVersionUID = 7915146919954537961L;
	private Boolean success = false;
	private String message;
	private Object data;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
	
}
