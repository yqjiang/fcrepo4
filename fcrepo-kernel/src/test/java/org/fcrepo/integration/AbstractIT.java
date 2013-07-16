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
package org.fcrepo.integration;

import static org.fcrepo.rdf.ExtractionUtils.addExtractor;
import static org.fcrepo.utils.FedoraJcrTypes.FEDORA_DATASTREAM;
import static org.fcrepo.utils.FedoraJcrTypes.FEDORA_OBJECT;
import static org.fcrepo.utils.FedoraJcrTypes.FEDORA_RESOURCE;
import static org.slf4j.LoggerFactory.getLogger;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractIT {

    protected Logger logger;

    @Before
    public void setLogger() {
        logger = getLogger(this.getClass());
    }

    public static void initExtractors(final Repository repo) throws Exception {
        final Session session = repo.login();
        addExtractor(session, FEDORA_RESOURCE,
                "org.fcrepo.rdf.impl.JcrGraphProperties");
        addExtractor(session, FEDORA_OBJECT,
                "org.fcrepo.rdf.impl.JcrGraphProperties");
        addExtractor(session, FEDORA_DATASTREAM,
                "org.fcrepo.rdf.impl.JcrGraphProperties");
        addExtractor(session, "nt:unstructured",
                "org.fcrepo.rdf.impl.JcrGraphProperties");
        session.save();
        session.logout();
    }
}
