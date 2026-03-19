package com.hamza.thymx.shared

import jakarta.validation.GroupSequence
import jakarta.validation.groups.Default

interface LaterValidationGroup

interface LastValidationGroup

@GroupSequence(Default::class, LaterValidationGroup::class, LastValidationGroup::class)
interface ValidationGroupSequence
