package research.old;

import database.JOOQDatabaseInteractor;

import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.GraphTraversalException;
import graph.RelationalGraphBFSHandler;
import org.jooq.Field;
import research.JoinInfo;
import research.RefactoredJoinTableConstruction;
import utils.NameGenerator;

import java.util.*;

public class JoinInfoLookahead extends RefactoredJoinTableConstruction {


    public JoinInfoLookahead(RelationalGraphBFSHandler relationalGraphBFSHandler, JOOQDatabaseInteractor jooqDatabaseInteractor, NameGenerator nameGenerator, Field targetIDField, TargetTableManager targetTableManager) {
        super(relationalGraphBFSHandler, jooqDatabaseInteractor, nameGenerator, targetIDField, targetTableManager);
    }

    /**
     * Returns JoinInfos for the given amount of lookaheadsteps
     * @param previousJoinInfo
     * @param lookaheadSteps
     * @return
     * @throws GraphTraversalException
     * @throws UnsupportedFeatureTransformationException
     * @throws UnsupportedFieldTypeException
     */
    public Optional<LinkedList<JoinInfo>> lookaheadJoinTableConstruction(JoinInfo previousJoinInfo, int lookaheadSteps,
                                                                         List<Object> instanceIDsTrainingSet)
            throws GraphTraversalException, UnsupportedFeatureTransformationException,  UnsupportedFieldTypeException {
        if(lookaheadSteps > 1){
            Set<JoinInfo> oneStepJoinInfoSet = oneStepJoinTableConstruction(previousJoinInfo, instanceIDsTrainingSet);

            LinkedList<JoinInfo> linkedJoinInfos = new LinkedList<>(oneStepJoinInfoSet);

            int newLookaheadSteps = lookaheadSteps - 1;
            for(JoinInfo joinInfo: oneStepJoinInfoSet) {
                Optional<LinkedList<JoinInfo>> lookaheadOptional = lookaheadJoinTableConstruction(joinInfo, newLookaheadSteps, instanceIDsTrainingSet);
                if(lookaheadOptional.isPresent()){
                    LinkedList<JoinInfo> lookaheadJoinInfos = lookaheadOptional.get();
                    linkedJoinInfos.addAll(lookaheadJoinInfos);
                }
            }
            return Optional.of(linkedJoinInfos);

        } else{
            return Optional.empty();
        }
    }


    public Set<JoinInfo> lookaheadOneStepJoinTableConstruction(Set<JoinInfo> joinInfos,
                                                               List<Object> instanceIDsTrainingSet)
            throws GraphTraversalException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
        Set<JoinInfo> newJoinInfos = new HashSet<>();

        for (JoinInfo joinInfo : joinInfos) {
            Set<JoinInfo> oneStepLookaheadForJoinInfo = oneStepJoinTableConstruction(joinInfo, instanceIDsTrainingSet);
            newJoinInfos.addAll(oneStepLookaheadForJoinInfo);
        }
        return newJoinInfos;
    }

}
