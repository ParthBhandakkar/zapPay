"""Add vehicle_id to qr_codes

Revision ID: f6a7b8c9d0e1
Revises: e5f6a7b8c9d0
Create Date: 2026-06-23

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'f6a7b8c9d0e1'
down_revision: Union[str, None] = 'e5f6a7b8c9d0'
branch_labels: Union[str, None] = None
depends_on: Union[str, None] = None


def upgrade() -> None:
    op.add_column('qr_codes', sa.Column('vehicle_id', sa.Integer(), nullable=True))
    op.create_foreign_key('fk_qr_codes_vehicle_id', 'qr_codes', 'user_vehicles',
                          ['vehicle_id'], ['id'])


def downgrade() -> None:
    op.drop_constraint('fk_qr_codes_vehicle_id', 'qr_codes', type_='foreignkey')
    op.drop_column('qr_codes', 'vehicle_id')
