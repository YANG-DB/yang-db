package com.yangdb.commons.task.repository;

/*-
 * #%L
 * commons
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.commons.allocation.InMemorySequenceIntIdAllocator;
import com.yangdb.commons.allocation.IntIdAllocator;
import com.yangdb.commons.task.Task;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryTaskRepository implements TaskRepository {
    //region Constructors
    public InMemoryTaskRepository() {
        this(new InMemorySequenceIntIdAllocator.Atomic(1));
    }

    public InMemoryTaskRepository(IntIdAllocator taskIdAllocator) {
        this.tasks = Collections.synchronizedMap(new HashMap<>());
        this.taskIdAllocator = taskIdAllocator;
    }
    //endregion

    //region TaskManager Implementation
    @Override
    public int size() {
        return this.tasks.size();
    }

    @Override
    public Collection<Task<?, ?>> getTasks() {
        return this.tasks.values();
    }

    @Override
    public <TTask extends Task<?, ?>> TTask getTask(int id) {
        return (TTask)this.tasks.get(id);
    }

    @Override
    public <TTask extends Task<?, ?>> TTask addTask(TTask task) {
        if (task.getId() == 0) {
            task.setId(this.taskIdAllocator.allocate());
        }

        this.tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public <TTask extends Task<?, ?>> TTask removeTask(int id) {
        return (TTask)this.tasks.remove(id);
    }
    //endregion

    //region Fields
    private Map<Integer, Task<?, ?>> tasks;
    private IntIdAllocator taskIdAllocator;
    //endregion
}
