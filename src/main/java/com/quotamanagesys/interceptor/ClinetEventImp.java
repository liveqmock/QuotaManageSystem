package com.quotamanagesys.interceptor;

import com.bstek.dorado.common.event.ClientEvent;

public class ClinetEventImp implements ClientEvent{
	
	private String script;

	@Override
	public String getScript() {
		// TODO Auto-generated method stub
		return this.script;
	}
	
	public void setScript(String script){
		this.script=script;
	}

}
