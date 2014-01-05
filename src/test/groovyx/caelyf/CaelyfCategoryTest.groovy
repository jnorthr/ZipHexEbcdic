package groovyx.caelyf

class CaelyfCategoryTest extends GroovyTestCase {

    void testMapToQueryString() {
        use(CaelyfCategory) {
            assert [:].toQueryString() == ""
            assert [title: 'Harry Potter'].toQueryString() == "title=Harry+Potter"
            assert [a: 1, b: 2].toQueryString() == "a=1&b=2"
            assert [first: 'this is a field', second: 'was it clear (already)?'].toQueryString() ==
                    "first=this+is+a+field&second=was+it+clear+%28already%29%3F"
        }
    }
}