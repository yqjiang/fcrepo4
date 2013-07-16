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

package org.fcrepo.rdf.impl;

import static org.fcrepo.utils.JcrPropertyStatementListener.getListener;
import static org.fcrepo.utils.JcrRdfTools.getGraphSubject;
import static org.fcrepo.utils.JcrRdfTools.getJcrPropertiesModel;
import static org.fcrepo.utils.JcrRdfTools.getJcrTreeModel;
import static org.fcrepo.utils.JcrRdfTools.getProblemsModel;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.fcrepo.rdf.GraphProperties;
import org.fcrepo.rdf.GraphSubjects;
import org.fcrepo.utils.JcrPropertyStatementListener;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


public class JcrGraphProperties implements GraphProperties {

    private static final String MODEL_NAME = "tree";

    @Override
    public String getPropertyModelName() {
        return MODEL_NAME;
    }

    @Override
    public Dataset getProperties(final Node node, final GraphSubjects subjects,
            final Dataset orig_dataset, final long offset, final int limit)
        throws RepositoryException {
        final Model model = getJcrPropertiesModel(subjects, node);
        final Model treeModel = getJcrTreeModel(subjects, node, offset, limit);
        final Model problemModel = getProblemsModel();

        final JcrPropertyStatementListener listener =
            getListener(subjects, node.getSession(), problemModel);

        model.register(listener);
        treeModel.register(listener);

        final Dataset dataset = DatasetFactory.create(model);
        dataset.addNamedModel(MODEL_NAME, treeModel);

        final Resource subject = getGraphSubject(subjects, node);
        final String uri = subject.getURI();
        final com.hp.hpl.jena.sparql.util.Context context =
            dataset.getContext();
        context.set(URI_SYMBOL, uri);

        dataset.addNamedModel(PROBLEMS_MODEL_NAME, problemModel);

        return dataset;
    }

    @Override
    public Dataset getProperties(final Node node, final GraphSubjects subjects,
        final Dataset orig_dataset) throws RepositoryException {
        return getProperties(node, subjects, orig_dataset, 0, -1);
    }

}
