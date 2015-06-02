(function() {
    'use strict';

    angular
        .module('angularApp')
        .controller('TestController', TestController);

    TestController.$inject = [];

    function TestController() {
        var vm = this;

        //var facility = Config.getFacilityByName(APP_CONFIG, 'dls');

        //var query = 'SELECT ins FROM Instrument ins, ins.facility f where f.id = ' + facility.facilityId + ' AND UPPER(ins.name) LIKE \'%B%\'';
        //http://localhost:3000/icat/entityManager?server=https://facilities02.esc.rl.ac.uk:8181&sessionId=0e8dc561-cf59-4640-b2e8-e5e8c5e8ceab&query=SELECT ins FROM Instrument ins, ins.facility f where f.id = 1 AND UPPER(ins.name) LIKE '%B%'
        //var countQuery = 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f where f.id = ' + facility.facilityId;

        var queries = [];



        try {
            queries.push(
                squel.select({ autoQuoteAliasNames: false })
                    .field('COUNT(ins)')
                    .from('Instrument', 'ins')
                    .from('ins.Instrument', 'f')
                    .where('f.id = ?', '1')
                    .toString()
            );
        } catch(error) {
            window.alert(error);
        }


        queries.push(
            squel.ICATSelect({ autoQuoteAliasNames: false })
                .include('f.facility')
                .include('i.investigation')
                .field('ins')
                .from('Instrument', 'ins')
                .where(
                    squel.expr()
                        .and('f.id = ?', '1')
                        .and('UPPER(ins.name) LIKE ?', '%b%')
                )
                .from('ins.Instrument', 'f')
                .order('dsds', false)
                .order('dsds', false)
                .toString()
        );

        //var query = 'SELECT ins FROM Instrument ins, ins.facility f where f.id = ' + facility.facilityId + ' AND UPPER(ins.name) LIKE \'%B%\'';
        //http://localhost:3000/icat/entityManager?server=https://facilities02.esc.rl.ac.uk:8181&sessionId=0e8dc561-cf59-4640-b2e8-e5e8c5e8ceab&query=SELECT ins FROM Instrument ins, ins.facility f where f.id = 1 AND UPPER(ins.name) LIKE '%B%'
        //var countQuery = 'SELECT COUNT(ins) FROM Instrument ins, ins.facility f where f.id = ' + facility.facilityId;
        //
        var myQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                .field('ins')
                .from('Instrument', 'ins')
                .from('ins.facility', 'f')
                .where(
                    squel.expr()
                        .and('f.id = ?', '1')
                )
                .order('ins.name', true);

        myQuery.where(
            squel.expr()
                .and('UPPER(ins.name) LIKE ?', '%b%')
        );

        queries.push(
            myQuery.toString()
        );

        /*queries.push(
            squel.pragma()
            .flush()
            .param('students')
                .toString()
        );*/

        vm.session = queries;
    }


})();
