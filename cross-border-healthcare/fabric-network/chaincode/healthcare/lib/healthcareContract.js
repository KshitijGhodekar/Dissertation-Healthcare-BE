'use strict';

const { Contract } = require('fabric-contract-api');

class HealthcareContract extends Contract {

    async initLedger(ctx) {
        console.info(' Ledger initialized');
    }

    async createPatient(ctx, id, name, age) {
        const patient = {
            name,
            age,
            docType: 'patient'
        };
        await ctx.stub.putState(id, Buffer.from(JSON.stringify(patient)));
        return `Patient ${name} created`;
    }

    async readPatient(ctx, id) {
        const data = await ctx.stub.getState(id);
        if (!data || data.length === 0) {
            throw new Error(` Patient with ID ${id} not found`);
        }
        return data.toString();
    }

    async getAllPatients(ctx) {
        const allResults = [];
        const iterator = await ctx.stub.getStateByRange('', '');

        while (true) {
            const res = await iterator.next();

            if (res.value && res.value.value.toString()) {
                const key = res.value.key;
                const value = res.value.value.toString('utf8');

                try {
                    const record = JSON.parse(value);
                    allResults.push({ Key: key, Record: record });
                } catch (err) {
                    allResults.push({ Key: key, Record: value });
                }
            }

            if (res.done) {
                await iterator.close();
                break;
            }
        }

        return JSON.stringify(allResults);
    }

    async deletePatient(ctx, id) {
        await ctx.stub.deleteState(id);
        return `Patient ${id} deleted`;
    }

    async verifyAccess(ctx, doctorId, patientId, purpose, hospital) {
        console.info(`Verifying access for doctor ${doctorId} to patient ${patientId} for ${purpose} at ${hospital}`);
        const result = {
            authorized: true,
            doctorId,
            patientId,
            purpose,
            hospital
        };

        return JSON.stringify(result);
    }

    async logAccess(ctx, doctorId, patientId, status, timestamp) {
        const logKey = `${doctorId}_${patientId}_${timestamp}`;
        const logEntry = {
            doctorId,
            patientId,
            status,
            timestamp,
            docType: 'accessLog'
        };

        await ctx.stub.putState(logKey, Buffer.from(JSON.stringify(logEntry)));
        return `Access logged for Doctor ${doctorId} on Patient ${patientId}`;
    }
}

module.exports = HealthcareContract;
