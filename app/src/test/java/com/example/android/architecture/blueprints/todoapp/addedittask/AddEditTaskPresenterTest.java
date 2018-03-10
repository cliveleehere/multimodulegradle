/*
 * Copyright 2016, The Android Open Source Project
 *
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
 */

package com.example.android.architecture.blueprints.todoapp.addedittask;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.ImmediateSchedulerProvider;
import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.Flowable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link AddEditTaskPresenter}.
 */
public class AddEditTaskPresenterTest {

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private AddEditTaskContract.View mAddEditTaskView;

    private BaseSchedulerProvider mSchedulerProvider;

    private AddEditTaskPresenter mAddEditTaskPresenter;

    @Before
    public void setupMocksAndView() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        mSchedulerProvider = new ImmediateSchedulerProvider();

        // The presenter wont't update the view unless it's active.
        when(mAddEditTaskView.isActive()).thenReturn(true);
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditTaskPresenter(
                null, mTasksRepository, mAddEditTaskView, true, mSchedulerProvider);

        // Then the presenter is set to the view
        verify(mAddEditTaskView).setPresenter(mAddEditTaskPresenter);
    }

    @Test
    public void saveNewTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditTaskPresenter(
                null, mTasksRepository, mAddEditTaskView, true, mSchedulerProvider);

        // When the presenter is asked to save a task
        mAddEditTaskPresenter.saveTask("New Task Title", "Some Task Description");

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveTask(any(Task.class)); // saved to the model
        verify(mAddEditTaskView).showTasksList(); // shown in the UI
    }

    @Test
    public void saveTask_emptyTaskShowsErrorUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditTaskPresenter(
                null, mTasksRepository, mAddEditTaskView, true, mSchedulerProvider);

        // When the presenter is asked to save an empty task
        mAddEditTaskPresenter.saveTask("", "");

        // Then an empty not error is shown in the UI
        verify(mAddEditTaskView).showEmptyTaskError();
    }

    @Test
    public void saveExistingTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditTaskPresenter(
                "1", mTasksRepository, mAddEditTaskView, true, mSchedulerProvider);

        // When the presenter is asked to save an existing task
        mAddEditTaskPresenter.saveTask("Existing Task Title", "Some Task Description");

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveTask(any(Task.class)); // saved to the model
        verify(mAddEditTaskView).showTasksList(); // shown in the UI
    }

    @Test
    public void populateTask_callsRepoAndUpdatesViewOnSuccess() {
        Task testTask = new Task("TITLE", "DESCRIPTION");
        Optional<Task> taskOptional = Optional.of(testTask);
        when(mTasksRepository.getTask(testTask.getId())).thenReturn(Flowable.just(taskOptional));

        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditTaskPresenter(testTask.getId(),
                mTasksRepository, mAddEditTaskView, true, mSchedulerProvider);

        // When the presenter is asked to populate an existing task
        mAddEditTaskPresenter.populateTask();

        // Then the task repository is queried and the view updated
        verify(mTasksRepository).getTask(eq(testTask.getId()));

        verify(mAddEditTaskView).setTitle(testTask.getTitle());
        verify(mAddEditTaskView).setDescription(testTask.getDescription());
        assertThat(mAddEditTaskPresenter.isDataMissing(), is(false));
    }

    @Test
    public void populateTask_callsRepoAndUpdatesViewOnAbsentTask() {
        Task testTask = new Task("TITLE", "DESCRIPTION");
        when(mTasksRepository.getTask(testTask.getId())).thenReturn(Flowable.just(Optional.absent()));

        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditTaskPresenter(testTask.getId(),
                mTasksRepository, mAddEditTaskView, true, mSchedulerProvider);

        // When the presenter is asked to populate an existing task
        mAddEditTaskPresenter.populateTask();

        // Then the task repository is queried and the view updated
        verify(mTasksRepository).getTask(eq(testTask.getId()));

        verify(mAddEditTaskView).showEmptyTaskError();
        verify(mAddEditTaskView, never()).setTitle(testTask.getTitle());
        verify(mAddEditTaskView, never()).setDescription(testTask.getDescription());
    }

    @Test
    public void populateTask_callsRepoAndUpdatesViewOnError() {
        Task testTask = new Task("TITLE", "DESCRIPTION");
        when(mTasksRepository.getTask(testTask.getId())).thenReturn(Flowable.error(new Throwable("Some error")));

        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditTaskPresenter(testTask.getId(),
                mTasksRepository, mAddEditTaskView, true, mSchedulerProvider);

        // When the presenter is asked to populate an existing task
        mAddEditTaskPresenter.populateTask();

        // Then the task repository is queried and the view updated
        verify(mTasksRepository).getTask(eq(testTask.getId()));

        verify(mAddEditTaskView).showEmptyTaskError();
        verify(mAddEditTaskView, never()).setTitle(testTask.getTitle());
        verify(mAddEditTaskView, never()).setDescription(testTask.getDescription());
    }
}
