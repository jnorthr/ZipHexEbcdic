/*
 * Copyright 2009-2011 the original author or authors.
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
package groovyx.caelyf.routes

import static groovyx.caelyf.TestUtil.request as r

/**
 * Tests for the routing support.
 *
 * @author Guillaume Laforge
 */
class RoutesTest extends GroovyTestCase {

    /** Tests the variable extraction logic */
    void testRoutesParameterExtraction() {
        def inputOutputExpected = [
            "/":                                [],
            "/*":                               [],
            "/**/*.*":                          [],
            "/*.*":                             [],
            "/**/@filename.*":                  ["@filename"],
            "/company/about":                   [],
            "/*/@from/*/@to":                   ["@from", "@to"],
            "/say/@from/to/@to":                ["@from", "@to"],
            "/script/@id":                      ["@id"],
            "/blog/@year/@month/@day/@title":   ["@year", "@month", "@day", "@title"],
            "/author/@author":                  ["@author"],
            "/tag/@tag":                        ["@tag"],
            "/@file.@extension":                ["@file", "@extension"],
            "/**/@file.@extension":             ["@file", "@extension"]
        ]

        inputOutputExpected.each { route, params ->
            assert Route.extractParameters(route) == params
        }
    }

    void testRegexRouteEquivalence() {
        def inputOutputExpected = [
            "/":                                /\//,
            "/*":                               /\/.+/,
            "/*.*":                             /\/.+\..+/,
            "/company/about":                   /\/company\/about/,
            "/*/@from/*/@to":                   /\/.+\/(.+)\/.+\/(.+)/,
            "/say/@from/to/@to":                /\/say\/(.+)\/to\/(.+)/,
            "/script/@id":                      /\/script\/(.+)/,
            "/blog/@year/@month/@day/@title":   /\/blog\/(.+)\/(.+)\/(.+)\/(.+)/,
            "/author/@author":                  /\/author\/(.+)/,
            "/tag/@tag":                        /\/tag\/(.+)/,
            "/@file.@extension":                /\/(.+)\.(.+)/,
            "/**":                              /\/(?:.+\/?){0,}/,
            "/**/@file.@extension":             /\/(?:.+\/?){0,}\/(.+)\.(.+)/,
            "/**/@filename.*":                  /\/(?:.+\/?){0,}\/(.+)\..+/,
            "/**/*.*":                          /\/(?:.+\/?){0,}\/.+\..+/,
        ]

        inputOutputExpected.each { route, regex ->
            assert Route.transformRouteIntoRegex(route) == regex
        }
    }

    void testRoutesAndVariableMatches() {
        def routeAndMatchingPaths = [
            "/blog/@year/@month/@day/@title":    [
                    "/blog/2009/11/27/Thanksgiving": ['year': '2009', 'month': '11', 'day': '27', 'title': 'Thanksgiving'],
                    "/blog/2008/03/04/birth": ['year': '2008', 'month': '03', 'day': '04', 'title': 'birth']
            ],
            "/**/@author/file/@file.@extension": [
                    "/foo/bar/glaforge/file/cv.doc": ['author': 'glaforge', 'file': 'cv', 'extension': 'doc']
            ],
            "/*.*": ["/cv.doc": [:]],
            "/company/about": ["/company/about": [:]],
            "/*/@from/*/@to": ["/groovy/glaforge/caelyf/me": ['from': 'glaforge', 'to': 'me']],
        ]

        routeAndMatchingPaths.each { String route, Map urisVariables ->
            urisVariables.each { String uri, Map variables ->
                def rt = new Route(route, "/destination")
                def result = rt.forUri(r(uri))
                assert result.matches
                assert result.variables == variables
            }
        }
    }

    void testNonMatchingRoute() {
        def rt = new Route("/somewhere", "/somewhere.groovy")
        assert !rt.forUri(r("/elsewhere")).matches
    }

    void testValidatorClosure() {
        def d = "/destination"
        def m = HttpMethod.GET
        def rt = RedirectionType.FORWARD

        assert new Route("/blog/@year", d, m, rt, { year.isNumber() }).forUri(r("/blog/2004")).matches
        assert !new Route("/blog/@year", d, m, rt, { year.isNumber() }).forUri(r("/blog/2004xxx")).matches

        assert new Route("/isbn/@isbn/toc", d, m, rt, { isbn ==~ /\d{9}(\d|X)/ }).forUri(r("/isbn/012345678X/toc")).matches
        assert !new Route("/isbn/@isbn/toc", d, m, rt, { isbn =~ /\d{9}(\d|X)/ }).forUri(r("/isbn/XYZ/toc")).matches

        assert new Route("/admin", d, m, rt, { request.user == 'USER' }).forUri(r("/admin")).matches
        assert !new Route("/admin", d, m, rt, { request.user == 'dummy' }).forUri(r("/admin")).matches
    }

    void testIgnoreRoute() {
        assert new Route("/ignore", null, HttpMethod.ALL, RedirectionType.FORWARD, null, null, 0, true).forUri(r("/ignore")).matches
    }

    void testRoutesWithParametersAndJSessionID() {
        def rt = new Route("/signup-user", "/signupUser.groovy")
        
        assert rt.forUri(r("/signup-user")).matches
        assert rt.forUri(r("/signup-user?login=failed")).matches
        assert rt.forUri(r("/signup-user;jsessionid=17o5jy7lz9t4t")).matches
        assert rt.forUri(r("/signup-user;jsessionid=17o5jy7lz9t4t?login=failed")).matches
    }
}
