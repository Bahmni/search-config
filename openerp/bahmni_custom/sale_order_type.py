import logging

from osv import fields, osv
import logging
_logger = logging.getLogger(__name__)


class sale_order(osv.osv):
    _name = "sale.order"
    _inherit = "sale.order"


    _columns={
        'care_setting': fields.selection([('opd', 'OPD'),('ipd', 'IPD')], 'Care Setting',required='True')
    }

sale_order()
