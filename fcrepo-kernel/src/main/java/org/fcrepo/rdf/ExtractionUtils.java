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

package org.fcrepo.rdf;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.transform;
import static org.fcrepo.utils.FedoraTypesUtils.node2name;
import static org.fcrepo.utils.NodeIterator.iterator;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.modeshape.jcr.api.JcrTools;
import org.slf4j.Logger;

import com.google.common.base.Function;

/**
 * Supports configurable triple extraction/enhancement.
 *
 * @author ajs6f
 * @date Jul 14, 2013
 */
public class ExtractionUtils {

    private static final Logger LOGGER = getLogger(ExtractionUtils.class);

    private static final String EXTRACTOR_CATALOG = "/fedora:extractors";



    /**
     * @param session
     * @param mixinName
     * @return
     * @throws RepositoryException
     */
    public static List<String> getExtractorNamesForMixin(
        final Session session, final String mixinName)
        throws RepositoryException {
        final Iterator<Node> extractors =
            iterator(session.getNode(EXTRACTOR_CATALOG + "/" + mixinName)
                    .getNodes());
        return transform(copyOf(extractors), node2name);
    }

    /**
     * Instantiates an extractor from a fully-qualified class name.
     */
    public static Function<String, GraphProperties> getExtractorForName =
        new Function<String, GraphProperties>() {

            @Override
            public GraphProperties apply(final String n) {
                try {
                    return (GraphProperties) Class.forName(n).newInstance();
                } catch (final ReflectiveOperationException e) {
                    LOGGER.error("Unable to create extractor for class: {}!", n);
                    propagate(e);
                    return null;
                }
            }
        };

    /**
     * @param session
     * @param mixinName
     * @return
     * @throws RepositoryException
     */
    public static List<GraphProperties> getExtractorsForMixin(
        final Session session, final String mixinName)
        throws RepositoryException {
        return transform(getExtractorNamesForMixin(session, mixinName),
                getExtractorForName);
    }

    /**
     * @param session
     * @param mixinName
     * @param extractorName
     * @throws RepositoryException
     */
    public static void addExtractor(final Session session,
        final String mixinName, final String extractorName)
        throws RepositoryException {
        new JcrTools().findOrCreateNode(session, EXTRACTOR_CATALOG + "/"
                + mixinName + "/" + extractorName);
    }

    /**
     * Prevent this class from being instantiated.
     */
    private ExtractionUtils() {
        throw new AssertionError(
                "This is a helper class which should not be instantiated!");
    }
}
