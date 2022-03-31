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

import com.yangdb.commons.task.Task;

import java.util.Collection;
import java.util.function.Consumer;

import static com.yangdb.commons.util.GenericUtils.infere;

public class ConclusiveTaskRepository implements TaskRepository {
    //region Constructors
    public ConclusiveTaskRepository(TaskRepository taskRepository, Consumer<Task<?, ?>> taskConclusionConsumer) {
        this.taskRepository = taskRepository;
        this.taskConclusionConsumer = taskConclusionConsumer;
    }
    //endregion

    //region TaskManager Implementation
    @Override
    public int size() {
        return this.taskRepository.size();
    }

    @Override
    public Collection<Task<?, ?>> getTasks() {
        return this.taskRepository.getTasks();
    }

    @Override
    public <TTask extends Task<?, ?>> TTask getTask(int id) {
        return this.taskRepository.getTask(id);
    }

    @Override
    public <TTask extends Task<?, ?>> TTask addTask(TTask task) {
        return infere(this.taskRepository.addTask(task).then(this.taskConclusionConsumer::accept));
    }

    @Override
    public <TTask extends Task<?, ?>> TTask removeTask(int id) {
        return this.taskRepository.removeTask(id);
    }
    //endregion

    //region Fields
    private final TaskRepository taskRepository;
    private final Consumer<Task<?, ?>> taskConclusionConsumer;
    //endregion
}
