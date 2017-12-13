package com.xiefayang.e_processVariables;

import java.io.Serializable;

public class Person implements Serializable{
	
	private static final long serialVersionUID = -4779339026623208281L;

	private Integer id;
	
	private String name;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
