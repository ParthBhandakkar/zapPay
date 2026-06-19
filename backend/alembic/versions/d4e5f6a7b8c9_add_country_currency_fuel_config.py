"""Add countries, currencies, fuel_products, feature_flags tables

Revision ID: d4e5f6a7b8c9
Revises: c3d4e5f6a7b8
Create Date: 2026-06-19

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = 'd4e5f6a7b8c9'
down_revision: Union[str, None] = 'c3d4e5f6a7b8'
branch_labels: Union[str, None] = None
depends_on: Union[str, None] = None


def upgrade() -> None:
    op.create_table('countries',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('code', sa.String(length=2), nullable=False),
        sa.Column('name', sa.String(length=100), nullable=False),
        sa.Column('phone_code', sa.String(length=5), nullable=True),
        sa.Column('currency_code', sa.String(length=3), nullable=False),
        sa.Column('default_language', sa.String(length=10), nullable=True),
        sa.Column('is_active', sa.Boolean(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('code'),
    )
    op.create_index(op.f('ix_countries_code'), 'countries', ['code'], unique=True)
    op.create_index(op.f('ix_countries_id'), 'countries', ['id'], unique=False)

    op.create_table('currencies',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('code', sa.String(length=3), nullable=False),
        sa.Column('name', sa.String(length=100), nullable=False),
        sa.Column('symbol', sa.String(length=10), nullable=False),
        sa.Column('decimal_places', sa.Integer(), nullable=True),
        sa.Column('is_active', sa.Boolean(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('code'),
    )
    op.create_index(op.f('ix_currencies_code'), 'currencies', ['code'], unique=True)
    op.create_index(op.f('ix_currencies_id'), 'currencies', ['id'], unique=False)

    op.create_table('fuel_products',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(length=100), nullable=False),
        sa.Column('country_code', sa.String(length=2), nullable=True),
        sa.Column('unit', sa.String(length=20), nullable=True),
        sa.Column('is_active', sa.Boolean(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.ForeignKeyConstraint(['country_code'], ['countries.code'], ),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_fuel_products_id'), 'fuel_products', ['id'], unique=False)

    op.create_table('feature_flags',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(length=100), nullable=False),
        sa.Column('enabled', sa.Boolean(), nullable=True),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('name'),
    )
    op.create_index(op.f('ix_feature_flags_id'), 'feature_flags', ['id'], unique=False)
    op.create_index(op.f('ix_feature_flags_name'), 'feature_flags', ['name'], unique=True)

    # Seed India and common currencies
    op.execute("""
        INSERT INTO countries (code, name, phone_code, currency_code, default_language, is_active)
        VALUES ('IN', 'India', '+91', 'INR', 'en', true)
        ON CONFLICT (code) DO NOTHING;
    """)
    op.execute("""
        INSERT INTO currencies (code, name, symbol, decimal_places, is_active)
        VALUES ('INR', 'Indian Rupee', '₹', 2, true)
        ON CONFLICT (code) DO NOTHING;
    """)
    op.execute("""
        INSERT INTO currencies (code, name, symbol, decimal_places, is_active)
        VALUES ('USD', 'US Dollar', '$', 2, true)
        ON CONFLICT (code) DO NOTHING;
    """)
    op.execute("""
        INSERT INTO currencies (code, name, symbol, decimal_places, is_active)
        VALUES ('EUR', 'Euro', '€', 2, true)
        ON CONFLICT (code) DO NOTHING;
    """)
    op.execute("""
        INSERT INTO fuel_products (name, country_code, unit, is_active)
        VALUES ('Petrol', 'IN', 'litre', true),
               ('Diesel', 'IN', 'litre', true),
               ('CNG', 'IN', 'kg', true)
        ON CONFLICT DO NOTHING;
    """)


def downgrade() -> None:
    op.drop_table('feature_flags')
    op.drop_table('fuel_products')
    op.drop_table('currencies')
    op.drop_table('countries')
