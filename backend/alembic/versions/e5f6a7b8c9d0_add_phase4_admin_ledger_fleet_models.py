"""Add Phase 4, admin, ledger, fleet models

Revision ID: e5f6a7b8c9d0
Revises: d4e5f6a7b8c9
Create Date: 2026-06-19

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'e5f6a7b8c9d0'
down_revision: Union[str, None] = 'd4e5f6a7b8c9'
branch_labels: Union[str, None] = None
depends_on: Union[str, None] = None


def upgrade() -> None:
    # ── Pump Fuel Prices ──
    op.create_table('pump_fuel_prices',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('pump_id', sa.Integer(), sa.ForeignKey('petrol_pumps.id'), nullable=False),
        sa.Column('fuel_type', sa.String(length=50), nullable=False),
        sa.Column('price', sa.Float(), nullable=False),
        sa.Column('effective_from', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.Column('effective_to', sa.DateTime(timezone=True), nullable=True),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_pump_fuel_prices_id'), 'pump_fuel_prices', ['id'], unique=False)
    op.create_index(op.f('ix_pump_fuel_prices_pump'), 'pump_fuel_prices', ['pump_id', 'fuel_type'], unique=False)

    # ── Pump Devices ──
    op.create_table('pump_devices',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('pump_id', sa.Integer(), sa.ForeignKey('petrol_pumps.id'), nullable=False),
        sa.Column('device_id', sa.String(length=255), nullable=False),
        sa.Column('device_name', sa.String(length=255), nullable=False),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.Column('last_seen', sa.DateTime(timezone=True), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('device_id'),
    )
    op.create_index(op.f('ix_pump_devices_id'), 'pump_devices', ['id'], unique=False)

    # ── Operator Shifts ──
    op.create_table('operator_shifts',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('pump_id', sa.Integer(), sa.ForeignKey('petrol_pumps.id'), nullable=False),
        sa.Column('operator_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('shift_type', sa.String(length=50), nullable=True),
        sa.Column('start_time', sa.DateTime(timezone=True), nullable=False),
        sa.Column('end_time', sa.DateTime(timezone=True), nullable=True),
        sa.Column('status', sa.String(length=50), server_default='active', nullable=True),
        sa.Column('notes', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_operator_shifts_id'), 'operator_shifts', ['id'], unique=False)

    # ── User Vehicles (multi-vehicle support) ──
    op.create_table('user_vehicles',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('vehicle_number', sa.String(length=20), nullable=False),
        sa.Column('vehicle_type', sa.String(length=50), nullable=True),
        sa.Column('nickname', sa.String(length=100), nullable=True),
        sa.Column('is_primary', sa.Boolean(), default=False),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_user_vehicles_id'), 'user_vehicles', ['id'], unique=False)
    op.create_index(op.f('ix_user_vehicles_user'), 'user_vehicles', ['user_id'], unique=False)

    # ── Pump Inventory ──
    op.create_table('pump_inventory',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('pump_id', sa.Integer(), sa.ForeignKey('petrol_pumps.id'), nullable=False),
        sa.Column('fuel_type', sa.String(length=50), nullable=False),
        sa.Column('current_stock', sa.Float(), default=0.0),
        sa.Column('max_capacity', sa.Float(), default=0.0),
        sa.Column('last_updated', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_pump_inventory_id'), 'pump_inventory', ['id'], unique=False)
    op.create_index(op.f('ix_pump_inventory_pump'), 'pump_inventory', ['pump_id', 'fuel_type'], unique=False)

    # ── Disputes ──
    op.create_table('disputes',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('transaction_id', sa.String(length=100), sa.ForeignKey('transactions.transaction_id'), nullable=False),
        sa.Column('customer_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('reason', sa.Text(), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('status', sa.String(length=50), server_default='open', nullable=True),
        sa.Column('resolved_by', sa.Integer(), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('resolution_notes', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.Column('resolved_at', sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_disputes_id'), 'disputes', ['id'], unique=False)

    # ── Notification Events ──
    op.create_table('notification_events',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('notification_type', sa.String(length=50), nullable=False),
        sa.Column('title', sa.String(length=255), nullable=False),
        sa.Column('body', sa.Text(), nullable=True),
        sa.Column('is_read', sa.Boolean(), default=False),
        sa.Column('is_sent', sa.Boolean(), default=False),
        sa.Column('sent_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('read_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_notification_events_id'), 'notification_events', ['id'], unique=False)
    op.create_index(op.f('ix_notification_events_user'), 'notification_events', ['user_id'], unique=False)

    # ── Support Tickets ──
    op.create_table('support_tickets',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('subject', sa.String(length=255), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('category', sa.String(length=50), nullable=True),
        sa.Column('priority', sa.String(length=50), server_default='medium', nullable=True),
        sa.Column('status', sa.String(length=50), server_default='open', nullable=True),
        sa.Column('assigned_to', sa.Integer(), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('resolution', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.Column('resolved_at', sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_support_tickets_id'), 'support_tickets', ['id'], unique=False)

    # ── Fraud Rules ──
    op.create_table('fraud_rules',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(length=255), nullable=False),
        sa.Column('rule_type', sa.String(length=50), nullable=False),
        sa.Column('rule_config', sa.Text(), nullable=True),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.Column('created_by', sa.Integer(), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.Column('updated_at', sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_fraud_rules_id'), 'fraud_rules', ['id'], unique=False)

    # ── Blacklist Entries ──
    op.create_table('blacklist_entries',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('pump_id', sa.Integer(), sa.ForeignKey('petrol_pumps.id'), nullable=True),
        sa.Column('reason', sa.Text(), nullable=False),
        sa.Column('blacklisted_by', sa.Integer(), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_blacklist_entries_id'), 'blacklist_entries', ['id'], unique=False)

    # ── Ledger Accounts ──
    op.create_table('ledger_accounts',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('account_type', sa.String(length=50), nullable=False),
        sa.Column('account_id', sa.Integer(), nullable=True),
        sa.Column('balance', sa.Float(), default=0.0),
        sa.Column('currency', sa.String(length=3), default='INR'),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_ledger_accounts_id'), 'ledger_accounts', ['id'], unique=False)
    op.create_index(op.f('ix_ledger_accounts_type'), 'ledger_accounts', ['account_type', 'account_id'], unique=False)

    # ── Ledger Entries ──
    op.create_table('ledger_entries',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('ledger_account_id', sa.Integer(), sa.ForeignKey('ledger_accounts.id'), nullable=False),
        sa.Column('transaction_id', sa.String(length=100), sa.ForeignKey('transactions.transaction_id'), nullable=True),
        sa.Column('entry_type', sa.String(length=20), nullable=False),
        sa.Column('amount', sa.Float(), nullable=False),
        sa.Column('balance_before', sa.Float(), nullable=True),
        sa.Column('balance_after', sa.Float(), nullable=True),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_ledger_entries_id'), 'ledger_entries', ['id'], unique=False)
    op.create_index(op.f('ix_ledger_entries_account'), 'ledger_entries', ['ledger_account_id'], unique=False)

    # ── Fleet Accounts ──
    op.create_table('fleet_accounts',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('company_name', sa.String(length=255), nullable=False),
        sa.Column('admin_user_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('monthly_budget', sa.Float(), default=0.0),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_fleet_accounts_id'), 'fleet_accounts', ['id'], unique=False)

    # ── Fleet Vehicles ──
    op.create_table('fleet_vehicles',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('fleet_id', sa.Integer(), sa.ForeignKey('fleet_accounts.id'), nullable=False),
        sa.Column('vehicle_number', sa.String(length=20), nullable=False),
        sa.Column('fuel_type', sa.String(length=50), nullable=True),
        sa.Column('monthly_fuel_limit', sa.Float(), default=0.0),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_fleet_vehicles_id'), 'fleet_vehicles', ['id'], unique=False)

    # ── Fleet Drivers ──
    op.create_table('fleet_drivers',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('fleet_id', sa.Integer(), sa.ForeignKey('fleet_accounts.id'), nullable=False),
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('daily_limit', sa.Float(), default=0.0),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_fleet_drivers_id'), 'fleet_drivers', ['id'], unique=False)


def downgrade() -> None:
    op.drop_table('fleet_drivers')
    op.drop_table('fleet_vehicles')
    op.drop_table('fleet_accounts')
    op.drop_table('ledger_entries')
    op.drop_table('ledger_accounts')
    op.drop_table('blacklist_entries')
    op.drop_table('fraud_rules')
    op.drop_table('support_tickets')
    op.drop_table('notification_events')
    op.drop_table('disputes')
    op.drop_table('pump_inventory')
    op.drop_table('user_vehicles')
    op.drop_table('operator_shifts')
    op.drop_table('pump_devices')
    op.drop_table('pump_fuel_prices')
