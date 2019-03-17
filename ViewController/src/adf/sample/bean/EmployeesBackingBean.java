package adf.sample.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import javax.el.MethodExpression;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import oracle.adf.view.rich.event.QueryEvent;
import oracle.adf.view.rich.model.AttributeCriterion;
import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.Criterion;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;

import oracle.jbo.domain.Number;

public class EmployeesBackingBean {
    public EmployeesBackingBean() {
    }

    /**
     * Custom Query Listener.
     * Applies af:selectMany choice values to the table filter criterion
     * @param queryEvent
     */
    public void onEmployeeTableQuery(QueryEvent queryEvent) {
        //user selected values
        ArrayList<Object> departmentIdArray = null;
        FilterableQueryDescriptor fqd = (FilterableQueryDescriptor) queryEvent.getDescriptor();

        //current criteria
        ConjunctionCriterion conjunctionCriterion = fqd.getFilterConjunctionCriterion();
        Map<String, Criterion> criterionMap = conjunctionCriterion.getCriterionMap();
        Criterion criterion = criterionMap.get("DepartmentId");

        //Translate DepartmentId array list to OR separate list of values
        StringBuffer deptIdFilterString = new StringBuffer();
        AttributeCriterion adfcriterion = null;
        // flag we set only if the DepartmentId filter is set (to reset the selection later)
        boolean flagDepIdFilter = false;
        if (criterion != null) {
            adfcriterion = (AttributeCriterion) criterion;
            Object object = adfcriterion.getValue();
            if (object != null) {
                flagDepIdFilter = true;
                departmentIdArray = (ArrayList<Object>) object;

                for (int argIndex = 0; argIndex < departmentIdArray.size(); argIndex++) {

                    //You need to know what is the underlying data type you are dealing
                    //with for the attribute. If you are on 11gR1 (11.1.1.x) then this
                    //type is jbo.domain.Number for numeric attributes.
                    //
                    //If you are on 11g R2 (11.1.2.x) this could be oracle.jbo.domain.Number,
                    //Integer or BigDecimal. If you use 11g R2, check the View Object for the
                    //attribute data type

                    if (argIndex == 0) {
                        //first argument has no OR

                        //this sample used oracle.jbo.domain.Number for the
                        //DepartmentId attribute
                        Number departmentId = (Number) departmentIdArray.get(argIndex);
                        deptIdFilterString.append(departmentId.toString());
                    } else {
                        //any subsequent argument is OR'ed together
                        deptIdFilterString.append(" OR ");
                        Number departmentId = (Number) departmentIdArray.get(argIndex);
                        deptIdFilterString.append(departmentId.toString());
                    }
                }
                //for some reasons, if in a single value select case, the
                //filter breaks and an error message is printed that the
                //String representation of the single value isn't found in
                //the list. The line below fixes the problem for filter values
                //that are positive numbers
                deptIdFilterString.append(" OR -1");
                String departmentIds = deptIdFilterString.toString();
                adfcriterion.setValue(departmentIds);
                fqd.setCurrentCriterion(adfcriterion);
            }
        }


        // preserve default query listener behavior
        //#{bindings.allEmployeesQuery.processQuery}

        FacesContext fctx = FacesContext.getCurrentInstance();
        Application application = fctx.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elctx = fctx.getELContext();

        MethodExpression methodExpression =
            expressionFactory.createMethodExpression(elctx, "#{bindings.allEmployeesQuery.processQuery}", Object.class,
                                                     new Class[] { QueryEvent.class });
        methodExpression.invoke(elctx, new Object[] { queryEvent });

        //restore filter selection done by the user. Note that this
        //needs to be saved as an ArrayList
        if (flagDepIdFilter) {
            adfcriterion.setValue(departmentIdArray);
            fqd.setCurrentCriterion(adfcriterion);
        }
    }
}
