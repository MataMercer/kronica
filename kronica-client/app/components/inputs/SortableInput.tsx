"use client";
//this is just a test.

import {
    DndContext,
    KeyboardSensor,
    MouseSensor,
    PointerSensor,
    TouchSensor,
    UniqueIdentifier,
    useSensor,
    useSensors,
} from "@dnd-kit/core";
import {
    arrayMove,
    rectSortingStrategy,
    SortableContext,
    sortableKeyboardCoordinates,
} from "@dnd-kit/sortable";
import { ReactElement, useEffect, useState } from "react";
import Draggable from "./Dnd/Draggable";
import SortableItem from "./Dnd/SortableItem";
import Droppable from "./Dnd/Droppable";

interface InputTypeInterface {
    id: UniqueIdentifier;
}

interface Props<T> {
    list: T[];
    setList: (arg0: T[]) => void;
    cardComponent: React.ComponentType<T>;
}

export default function SortableInput<T extends InputTypeInterface>({
    list,
    setList,
    cardComponent,
}: Props<T>) {
    const mouseSensor = useSensor(MouseSensor);
    const touchSensor = useSensor(TouchSensor);
    const keyboardSensor = useSensor(KeyboardSensor);

    const sensors = useSensors(mouseSensor, touchSensor, keyboardSensor);

    mouseSensor.sensor.activators = [
        {
            eventName: "onPointerDown",
            handler: ({ nativeEvent: event }) => {
                if (
                    !event.isPrimary ||
                    event.button !== 0 ||
                    isInteractiveElement(event.target)
                ) {
                    return false;
                }

                return true;
            },
        },
    ];

    function isInteractiveElement(element: HTMLElement) {
        const interactiveElements = [
            "button",
            "input",
            "textarea",
            "select",
            "option",
        ];

        if (interactiveElements.includes(element.tagName.toLowerCase())) {
            return true;
        }

        return false;
    }

    function handleDragEnd({ active, over }: any) {
        if (!over) {
            return;
        }

        if (active.id !== over.id) {
            const activeIndex = active.data.current.sortable.index;
            const overIndex = over.data.current?.sortable.index || 0;
            setList(arrayMove(list, activeIndex, overIndex));
        }
    }

    function handleRemoveClick(event: any) {
        const index = parseInt(event?.target?.value);
        setList(list.toSpliced(index, 1));
    }

    const CardComponent = cardComponent as any;
    return (
        <DndContext sensors={sensors} onDragEnd={handleDragEnd}>
            <SortableContext items={list} strategy={rectSortingStrategy}>
                <Droppable>
                    {list.map((item, index) => (
                        <SortableItem key={item.id} id={item.id}>
                            <CardComponent {...item} />
                            <button
                                className="p-10 bg-black text-white"
                                onClick={handleRemoveClick}
                                type="button"
                                value={index}
                            >
                                X
                            </button>
                        </SortableItem>
                    ))}
                </Droppable>
            </SortableContext>
        </DndContext>
    );
}
