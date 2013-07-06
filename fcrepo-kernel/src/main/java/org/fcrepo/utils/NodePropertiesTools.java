/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.utils;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Arrays.asList;
import static javax.jcr.PropertyType.UNDEFINED;
import static javax.jcr.PropertyType.nameFromValue;
import static org.fcrepo.utils.FedoraTypesUtils.getDefinitionForPropertyName;
import static org.fcrepo.utils.FedoraTypesUtils.isMultipleValuedProperty;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;

import org.slf4j.Logger;

/**
 * Tools for replacing, appending and deleting JCR node properties
 *
 * @author Chris Beer
 * @date May 10, 2013
 */
public final class NodePropertiesTools {

    private static final Logger logger = getLogger(NodePropertiesTools.class);

    private NodePropertiesTools() {
        throw new AssertionError(
                "NodePropertiesTools is a helper class which should not be instantiated!");

    }

    /**
     * Given a JCR node, property and value, either: - if the property is
     * single-valued, replace the existing property with the new value - if the
     * property is multivalued, append the new value to the property
     *
     * @param node the JCR node
     * @param propertyName a name of a JCR property (either pre-existing or
     *        otherwise)
     * @param newValue the JCR value to insert
     * @throws RepositoryException
     */
    public static void appendOrReplaceNodeProperty(final Node node,
            final String propertyName, final Value newValue)
        throws RepositoryException {

        // if it already exists, we can take some shortcuts
        if (node.hasProperty(propertyName)) {

            final Property property = node.getProperty(propertyName);

            if (property.isMultiple()) {
                logger.debug("Appending value {} to {} property {}", newValue,
                        nameFromValue(property.getType()), propertyName);

                // if the property is multi-valued, go ahead and append to it.
                final List<Value> newValues =
                    new ArrayList<Value>(asList(node.getProperty(propertyName)
                            .getValues()));

                if (!newValues.contains(newValue)) {
                    newValues.add(newValue);
                    property.setValue(newValues.toArray(new Value[newValues
                            .size()]));
                }
            } else {
                // or we'll just overwrite it
                logger.debug("Overwriting {} property {} with new value {}",
                        nameFromValue(property.getType()), propertyName,
                        newValue);
                property.setValue(newValue);
            }
        } else {
            if (isMultivaluedProperty(node, propertyName)) {
                logger.debug("Creating new multivalued {} property {} with "
                        + "initial value [{}]", nameFromValue(newValue
                        .getType()), propertyName, newValue);
                node.setProperty(propertyName, new Value[] {newValue});
            } else {
                logger.debug("Creating new single-valued {} property {} with "
                        + "initial value {}",
                        nameFromValue(newValue.getType()), propertyName,
                        newValue);
                node.setProperty(propertyName, newValue);
            }
        }

    }

    /**
     * Given a JCR node, property and value, remove the value (if extant)
     * from the property
     *
     * @param node the JCR node
     * @param propertyName a name of a JCR property (pre-existing or not)
     * @param valueToRemove the JCR value to remove
     * @throws RepositoryException
     */
    public static void removeNodeProperty(final Node node,
            final String propertyName, final Value valueToRemove)
        throws RepositoryException {
        // if the property doesn't exist, we don't need to worry about it.
        if (node.hasProperty(propertyName)) {

            final Property property = node.getProperty(propertyName);

            if (isMultipleValuedProperty.apply(property)) {

                final List<Value> oldValues =
                    copyOf(node.getProperty(propertyName).getValues());

                // we only need to update the property if we do anything.
                if (oldValues.contains(valueToRemove)) {
                    final Collection<Value> newValues =
                        filter(oldValues, not(equalTo(valueToRemove)));
                    if (newValues.size() == 0) {
                        logger.debug("Removing property {}", propertyName);
                        property.remove();
                    } else {
                        logger.debug("Removing value {} from property {}",
                                valueToRemove, propertyName);
                        property.setValue(newValues.toArray(new Value[newValues
                                .size()]));
                    }
                }
            } else {
                if (property.getValue().equals(valueToRemove)) {
                    logger.debug("Removing value {} from property {}",
                            valueToRemove, propertyName);
                    property.remove();
                }
            }
        }
    }

    /**
     * Get the JCR property type ID for a given property name. If unsure, mark
     * it as UNDEFINED.
     *
     * @param node the JCR node to add the property on
     * @param propertyName the property name
     * @return a PropertyType value
     * @throws RepositoryException
     */
    public static int getPropertyType(final Node node,
        final String propertyName) throws RepositoryException {
        final PropertyDefinition def =
                getDefinitionForPropertyName(node, propertyName);
        if (def == null) {
            return UNDEFINED;
        }
        return def.getRequiredType();
    }

    /**
     * Determine if a given JCR property name is single- or multi- valued. If
     * unsure, choose the least restrictive option (multivalued)
     *
     * @param node the JCR node to check
     * @param propertyName the property name (which may or may not already
     *        exist)
     * @return true if the property is (or could be) multivalued
     * @throws RepositoryException
     */
    public static boolean isMultivaluedProperty(final Node node,
            final String propertyName) throws RepositoryException {
        final PropertyDefinition def =
                getDefinitionForPropertyName(node, propertyName);
        if (def == null) {
            return true;
        }
        return def.isMultiple();
    }

}
