/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.test.api.cmmn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CaseServiceStageTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testManualStart() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
      .withCaseDefinition(caseDefinitionId)
      .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
      .activityId("PI_Stage_1")
      .singleResult()
      .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // there exists two new case execution:

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testManualStartWithVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariable("aVariableName", "abc")
      .setVariable("anotherVariableName", 999)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testManualWithVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariables(variables)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseInstanceId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testManualStartWithLocalVariable() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariableLocal("aVariableName", "abc")
      .setVariableLocal("anotherVariableName", 999)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testManualStartWithLocalVariables() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    // an enabled child case execution of
    // the case instance
    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", "abc");
    variables.put("anotherVariableName", 999);

    // when
    // activate child case execution
    caseService
      .withCaseExecution(caseExecutionId)
      .setVariablesLocal(variables)
      .manualStart();

    // then

    // the child case execution is active...
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    assertTrue(caseExecution.isActive());
    // ... and not enabled
    assertFalse(caseExecution.isEnabled());

    // (1) one case case execution representing "PI_HumanTask_1"
    CaseExecution firstHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(firstHumanTask);
    assertTrue(firstHumanTask.isEnabled());
    assertFalse(firstHumanTask.isActive());

    // (2) one case case execution representing "PI_HumanTask_2"
    CaseExecution secondHumanTask = caseExecutionQuery
        .activityId("PI_HumanTask_2")
        .singleResult();

    assertNotNull(secondHumanTask);
    assertTrue(secondHumanTask.isEnabled());
    assertFalse(secondHumanTask.isActive());

    // the case instance has two variables:
    // - aVariableName
    // - anotherVariableName
    List<VariableInstance> result = runtimeService
        .createVariableInstanceQuery()
        .list();

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    for (VariableInstance variable : result) {

      assertEquals(caseExecutionId, variable.getCaseExecutionId());
      assertEquals(caseInstanceId, variable.getCaseInstanceId());

      if (variable.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variable.getName());
        assertEquals("abc", variable.getValue());
      } else if (variable.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variable.getName());
        assertEquals(999, variable.getValue());
      } else {
        fail("Unexpected variable: " + variable.getName());
      }
    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testReenableAnEnabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .reenable();
      fail("It should not be possible to re-enable an enabled stage.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testReenableAnDisabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // the human task is disabled
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .reenable();

    // then
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    // the human task is disabled
    assertFalse(caseExecution.isDisabled());
    assertFalse(caseExecution.isActive());
    assertTrue(caseExecution.isEnabled());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testReenableAnActiveStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when
    try {
      caseService
        .withCaseExecution(caseExecutionId)
        .reenable();
      fail("It should not be possible to re-enable an active human task.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testDisableAnEnabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance and the containing
    // human task is enabled
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // then
    CaseExecution caseExecution = caseExecutionQuery.singleResult();
    // the human task is disabled
    assertTrue(caseExecution.isDisabled());
    assertFalse(caseExecution.isActive());
    assertFalse(caseExecution.isEnabled());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testDisableADisabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    CaseExecutionQuery caseExecutionQuery = caseService.createCaseExecutionQuery();

    String caseExecutionId = caseExecutionQuery
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // the human task is disabled
    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .disable();
      fail("It should not be possible to disable a already disabled human task.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testDisableAnActiveStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when
    try {
      caseService
        .withCaseExecution(caseExecutionId)
        .disable();
      fail("It should not be possible to disable an active human task.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testManualStartOfADisabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .manualStart();
      fail("It should not be possible to start a disabled human task manually.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testManualStartOfAnActiveStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
        .withCaseDefinition(caseDefinitionId)
        .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .manualStart();
      fail("It should not be possible to start an already active human task manually.");
    } catch (Exception e) {
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testDisableShouldCompleteCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance has been completed
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .completed()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testCompleteShouldCompleteCaseInstance() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .complete();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance has been completed
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .completed()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testComplete() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // when

    caseService
      .withCaseExecution(caseExecutionId)
      .complete();

    // then

    // the corresponding case execution has been also
    // deleted and completed
    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNull(caseExecution);

    // the case instance is still active
    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .active()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isActive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testCompleteEnabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .complete();
      fail("Should not be able to complete stage.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageCase.cmmn"})
  public void testCompleteDisabledStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .disable();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .complete();
      fail("Should not be able to complete stage.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyStageCase.cmmn"})
  public void testAutoCompletionOfEmptyStage() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    // then

    CaseExecution caseExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNull(caseExecution);

    CaseInstance caseInstance = caseService
      .createCaseInstanceQuery()
      .completed()
      .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneStageCase.cmmn"})
  public void testClose() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    try {
      // when
      caseService
        .withCaseExecution(caseExecutionId)
        .close();
      fail("It should not be possible to close a stage.");
    } catch (ProcessEngineException e) {

    }

  }

}