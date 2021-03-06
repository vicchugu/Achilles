/*
 * Copyright (C) 2012-2014 DuyHai DOAN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package info.archinnov.achilles.test.integration.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Strategy;
import info.archinnov.achilles.type.NamingStrategy;

import static info.archinnov.achilles.test.integration.entity.ChildEntity.TABLE_NAME;

@Entity(table = TABLE_NAME)
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class ChildEntity extends ParentEntity{
    public static final String TABLE_NAME = "child_entity";

    @Column
    private String childValue;

    public ChildEntity() {
    }

    public ChildEntity(Long id, String parentValue, String childValue) {
        super(id,parentValue);
        this.childValue = childValue;
    }

    public String getChildValue() {
        return childValue;
    }

    public void setChildValue(String childValue) {
        this.childValue = childValue;
    }
}
