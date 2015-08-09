package mongodb

class Organization {
        String id

        static constraints = {
                id nullable: false, index: true
        }
}

