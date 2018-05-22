/*
 * Copyright (c) 2016-2088, fastquery.org and/or its affiliates. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For more information, please see http://www.fastquery.org/.
 * 
 */

package org.fastquery.filter;

import java.lang.reflect.Method;

import org.fastquery.core.Repository;

/**
 * 
 * @author xixifeng (fastquery@126.com)
 */
public abstract class BeforeFilter<R extends Repository> {

	/**
	 * @param repository 当前拦截到的repository实例
	 * @param method 当前拦截到的方法
	 * @param args 客户传递进来的参数
	 */
	protected abstract void doFilter(R repository,Method method, Object[] args);
	
	/**
	 * 终止链条,直接返回returnVal
	 * @param returnVal 放回的值
	 */
	protected void abortWith(Object returnVal){
		BeforeFilterChain.setThreadLocal(returnVal);
	}
}
