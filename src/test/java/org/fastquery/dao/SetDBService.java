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

package org.fastquery.dao;

import org.fastquery.bean.Course;
import org.fastquery.core.Modifying;
import org.fastquery.core.Query;
import org.fastquery.core.QueryRepository;
import org.fastquery.where.Set;

/**
 * 
 * @author mei.sir@aliyun.cn
 */
public interface SetDBService extends QueryRepository {

	@Modifying
	@Query("update `Course` #{#sets} where no = ?5")
	@Set("`name` = ?1") // ?1 若是 null 或是 "" , 则, 该行set被移除
	@Set("`credit` = ?2")
	@Set("`semester` = ?3")
	@Set("`period` = ?4")
	int updateCourse(String name,Integer credit, Integer semester, Integer period, String no);
	
	@Query("select * from Course where no = ?1")
	Course findCourse(String no);
	
}
