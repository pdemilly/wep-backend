package crs.core

import mongodb.*

class CrsService {

        def org = new Organization ()

        Organization getOrganization () {
                return org
        }
}
