/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
define('ext/image', ['ext/define'], function(){

    /**
     * A simple component to display an image.
     *
     * @class Ext.Image
     * @extends Ext.BoxComponent
     * @namespace Ext
     */
    Ext.define('Ext.Image', {
        extend: 'Ext.BoxComponent',

        /**
         * @cfg {String} src Image source
         */
        constructor: function (config) {
            var self = this;

            Ext.apply(self, {
                autoEl: {
                    tag: 'img',
                    src: config.src
                }
            });

            // Call super constructor
            self.constructor.superclass.constructor.apply(self, arguments);
        }
    },

    function () {
        var type = this;
        Ext.ComponentMgr.registerType('image', type);
    });

});
