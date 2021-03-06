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

package org.fastquery.asm;

import java.lang.reflect.Method;

import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.core.QueryByNamed;
import org.fastquery.core.QueryRepository;
import org.fastquery.core.Repository;
import org.fastquery.core.RepositoryException;
import org.fastquery.filter.generate.common.MethodFilterChain;
import org.fastquery.filter.generate.global.InterceptorFilter;
import org.fastquery.filter.generate.global.PageableFilter;
import org.fastquery.filter.generate.global.ReturnTypeFilter;
import org.fastquery.filter.generate.global.SharpFilter;
import org.fastquery.filter.generate.modifying.AnnotationSynxFilter;
import org.fastquery.filter.generate.modifying.ArgsFilter;
import org.fastquery.filter.generate.modifying.ModifyingReturnTypeFilter;
import org.fastquery.filter.generate.query.NotAllowedRepeat;
import org.fastquery.filter.generate.query.PageFilter;
import org.fastquery.filter.generate.query.ParameterFilter;
import org.fastquery.filter.generate.query.QueryReturnTypeFilter;
import org.fastquery.filter.generate.query.SQLFilter;
import org.fastquery.filter.generate.querya.ConditionParameterFilter;
import org.fastquery.filter.generate.querya.MarkFilter;
import org.fastquery.filter.generate.querya.MethodAnnotationFilter;
import org.fastquery.filter.generate.querya.ModifyingDependencyFilter;
import org.fastquery.filter.generate.querya.MuestionFilter;
import org.fastquery.filter.generate.querya.OutFilter;
import org.fastquery.filter.generate.querya.SourceFilter;
import org.fastquery.filter.generate.queryn.QueriesFileFilter;
import org.fastquery.filter.generate.queryn.TplPageFilter;

/**
 * 生成 Repository的实现扩展, 在生成的时候,额外要做的事情(extend)
 * 
 * @author xixifeng (fastquery@126.com)
 */
// 该方法只为AsmRepository 服务
class GenerateExtends {

	private GenerateExtends() {
	}

	/**
	 * 在生成Repository实现类之前,做安全检测
	 * 
	 * @param repositoryClazz
	 */
	static void safeCheck(Class<? extends Repository> repositoryClazz) {

		// 要时能做sql语法检测,预处理,那就好了!!!

		// 创建一个公共过滤链条
		MethodFilterChain globalFilterChain;
		// 增加公共过滤器

		// 主要是针对QueryRepository的过滤链条
		MethodFilterChain queryFilterChain;

		Modifying modifying;
		Query[] querys;
		QueryByNamed queryByNamed;

		Method[] methods = repositoryClazz.getMethods();
		for (Method method : methods) {

			Class<?> declaringClass = method.getDeclaringClass();

			// 规范接口中的方法不参与校验
			if (declaringClass == QueryRepository.class || declaringClass == Repository.class) {
				continue;
			}

			modifying = method.getAnnotation(Modifying.class);
			querys = method.getAnnotationsByType(Query.class);
			queryByNamed = method.getAnnotation(QueryByNamed.class);

			// 对过滤器进行分类
			// filter/global 拦截全局的方法
			// filter/mqueryn 拦截标注有@QueryByNamed @modifying的方法,并且是QueryRepository的实现方法
			// filter/queryn 拦截既标注有@QueryByNamed,没有标注@modifying的方法, 并且是QueryRepository的实现方法
			// filter/modifying 拦截标注有@Query @modifying的方法,并且是QueryRepository的实现方法
			// filter/mquery 拦截既没有标注@Query,又没有标注@modifying的方法, 并且是QueryRepository的实现方法
			// filter/query 拦截既标注有@Query,没有标注@modifying的方法, 并且是QueryRepository的实现方法
			// filter/querya 拦截QueryRepository的实现方法

			// 全局责任链
			// filter/global
			globalFilterChain = new MethodFilterChain();
			globalFilterChain.addFilter(new ReturnTypeFilter());
			globalFilterChain.addFilter(new InterceptorFilter()); // @Before,@After拦截器安全校验
			globalFilterChain.addFilter(new PageableFilter());
			globalFilterChain.addFilter(new SharpFilter()); // #{#表达式} 合法检测

			queryFilterChain = new MethodFilterChain();

			if (QueryRepository.class.isAssignableFrom(repositoryClazz)) { // 若:QueryRepository 是
																			// repositoryClazz的父类

				// filter/querya
				queryFilterChain.addFilter(new ModifyingDependencyFilter());
				queryFilterChain.addFilter(new MethodAnnotationFilter());
				queryFilterChain.addFilter(new SQLFilter());
				queryFilterChain.addFilter(new ConditionParameterFilter());
				queryFilterChain.addFilter(new SourceFilter());
				queryFilterChain.addFilter(new OutFilter());
				queryFilterChain.addFilter(new MarkFilter());
				queryFilterChain.addFilter(new MuestionFilter());

				// filter/modifying
				if (modifying != null && querys.length > 0) {
					queryFilterChain.addFilter(new AnnotationSynxFilter());
					queryFilterChain.addFilter(new ArgsFilter());
					queryFilterChain.addFilter(new ModifyingReturnTypeFilter());
				}

				// filter/query
				if (querys.length > 0 && modifying == null) {
					queryFilterChain.addFilter(new QueryReturnTypeFilter());
					queryFilterChain.addFilter(new ParameterFilter());
					queryFilterChain.addFilter(new NotAllowedRepeat());
					queryFilterChain.addFilter(new PageFilter());
				}

				// filter/mqueryn
				if (modifying != null && queryByNamed != null) {

				}
				// filter/queryn
				if (queryByNamed != null && modifying == null) {
					queryFilterChain.addFilter(new TplPageFilter());
					queryFilterChain.addFilter(new QueriesFileFilter());
				}

				// filter/mquery
				if (querys.length == 0 && modifying == null) {
					// 有待扩展...
				}

			} else {
				throw new RepositoryException(repositoryClazz + "不能解析");
			}

			// 把责任链条连接起来
			// 多根链接衔接完毕之后就执行过滤
			globalFilterChain.addFilter(queryFilterChain).doFilter(method);

		}
	}
}
